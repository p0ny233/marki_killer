package com.hook;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.provider.Settings.Secure;
import com.utils.Tools;
import com.utils.server.SocketServerTools;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage {

    private static Context ctx;

    private static SocketServerTools mSocketServerTools;

    private static String android_id;
    private static String origin_android_id;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Hook 应用，当应用启动后，会执行 handleLoadPackage函数
        Log.i("yrx ->", "handleLoadPackage, packageName: " + lpparam.packageName
                + ", processname: " + lpparam.processName);

        if (lpparam.packageName.equals("com.ai.marki") && lpparam.processName.equals("com.ai.marki")) {
            /*
                匹配包名和进程名，严格过滤
             */
            // 启动Socket服务端线程
            if (mSocketServerTools == null) {
                mSocketServerTools = new SocketServerTools();
                mSocketServerTools.start();
            }

            // 获取 Context
            XposedHelpers.findAndHookMethod(
                    Class.forName("android.app.Application"),
                    "attach",  // final void attach(Context context) {
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (ctx == null){
                                XposedBridge.log("xposed_module, 获取到ctx");
                                ctx = (Context) param.args[0];
                                new Tools.Inner(ctx);
                            }
                        }
                    }
            );

            /*
                验真次数, 修改返回值
             */
            XposedHelpers.findAndHookMethod(
                    Class.forName("android.provider.Settings$Secure"),  // 内部类
                    "getString",
                    ContentResolver.class,
                    String.class,
                    new XC_MethodHook(){
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            // 生成随机id

                            if (android_id == null)
                                android_id = Tools.generateAndroid_ID(16);

                            Object obj = param.getResult();

                            if (origin_android_id == null){
                                obj = param.getResult();
                                if (obj == null)
                                    return;
                                origin_android_id =  ((String)obj).length() == 16 ? (String) param.getResult() : null ;
                                XposedBridge.log("xposed_module, origin_android_id: " + origin_android_id);

                            }

                            XposedBridge.log("xposed_module, getString 原结果: " + obj);
                            if (obj == null){
                                return;
                            }

                            if (param.getResult().toString().equals(origin_android_id)){
                                XposedBridge.log("xposed_module, 原android_id: " + param.getResult() + " , 修改后: " + android_id);
                                param.setResult(android_id);
                            }
                        }
                    }
            );

            /*
                自定义水印时间
             */
            XposedHelpers.findAndHookConstructor(
                    "com.ai.marki.common.util.n1",
                    lpparam.classLoader,
                    long.class, // 第一个参数类型
                    long.class, // 第二个参数类型
                    new XC_MethodHook() { // 回调
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            long time = Tools.getTargetTime();
                            XposedBridge.log("xposed_module, 时间戳: " + time + ", 时间: " + Tools.formatTime(time));
                            if (time != 0){
                                param.args[0] = time;  // 修改 有参构造函数的参数值
                            }
                        }
                    }
            );
        }
    }
}
