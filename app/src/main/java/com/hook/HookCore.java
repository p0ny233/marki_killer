package com.hook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.utils.Tools;
import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HookCore {

    /*
        获取 Context的 回调函数
     */
    public static XC_MethodHook hookCodeForgetContext(){
        return new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (HookEntry.getCtx() == null){
                    XposedBridge.log("xposed_module, 获取到ctx");
                    Context ctx = (Context) param.args[0];
                    HookEntry.setCtx(ctx);
                    new Tools.Inner(ctx);
                }
            }
        };
    }

    /*
        重置 Android id，实现无限次数 验真(需要清除App的全部数据才有效果)
     */
    public static XC_MethodHook hookCodeForResetAndroidId(){
        return new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                // 生成随机id
                if (HookEntry.getAndroidId() == null){
                    String android_id = Tools.generateAndroid_ID(16);
                    HookEntry.setAndroidId(android_id);
                }

                Object obj = param.getResult();

                if (HookEntry.getOriginAndroidId() == null){
                    obj = param.getResult();
                    if (obj == null)
                        return;
                    String origin_android_id =  ((String)obj).length() == 16 ? (String) param.getResult() : null ;
                    HookEntry.setOriginAndroId(origin_android_id);
                    XposedBridge.log("xposed_module, origin_android_id: " + origin_android_id);
                }

                XposedBridge.log("xposed_module, getString 原结果: " + obj);
                if (obj == null){
                    return;
                }

                if (param.getResult().toString().equals(HookEntry.getOriginAndroidId())){
                    XposedBridge.log("xposed_module, 原android_id: " + param.getResult() + " , 修改后: " + HookEntry.getAndroidId());
                    param.setResult(HookEntry.getAndroidId());
                }
            }
        };

    }

    /*
        自定义水印时间
     */
    public static XC_MethodHook hookCodeForCustomTimeStamp(){
        return new XC_MethodHook() { // 回调
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                long time = Tools.getTargetTime();
                XposedBridge.log("xposed_module, 时间戳: " + time + ", 时间: " + Tools.formatTime(time));
                if (time != 0){
                    param.args[0] = time;  // 修改 有参构造函数的参数值
                }
            }
        };
    }


    /*
        替换照片 android.graphics.BitmapFactory.decodeStream.overload("java.io.InputStream").
     */
    public static XC_MethodHook hookCodeForReplacePic(){
        return new XC_MethodHook() { // 回调
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                // 根据 开关决定是否 启用图片替换
                if(!Tools.getIsReplacePic())
                    return;

                // 读取文件
                String path = Tools.getPicPath();
                File file = new File(path);

                if (!file.exists()){
                    XposedBridge.log("xposed_module, 替换图片路径不存在, path = [" + path + "]");
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeFile(path);
                param.setResult(bmp);
                XposedBridge.log("xposed_module, 替换图片成功, path = [" + path + "]");
            }
        };
    }

    /*
        替换图片时。按照app的逻辑，默认是开启自拍镜像，得到一张角度正常的照片
        当我仅用frida 去替换照片，得到一张水平翻转的照片

            拍摄 -> 翻转 -> 正常照片
            拍摄 -> 替换 -> 翻转 -> 翻转照片
        因此控制不要去翻转即可
     */
    public static XC_MethodHook hookCodeForCcameraMirror(){
        return new XC_MethodHook() { // 回调
            private boolean isReplace;
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Tools.getIsReplacePic())
                    if (((String)param.args[0]).equals("key_camera_mirror")){
                        isReplace = true;
                    }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (isReplace)
                    param.setResult(false);
                isReplace = false;
            }
        };
    }
}
