package com.hook;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import com.utils.server.SocketServerTools;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
public class HookEntry implements IXposedHookLoadPackage {

    private static Context ctx;

    private static SocketServerTools mSocketServerTools;

    private static String android_id;
    private static String origin_android_id;

    public HookEntry(){

    }

    public static void setAndroidId(String id){
        android_id = id;
    }
    public static String getAndroidId(){
        return android_id;
    }

    public static void setOriginAndroId(String id){
        origin_android_id = id;
    }
    public static String getOriginAndroidId(){
        return origin_android_id;
    }

        public static void setCtx(Context c){
        ctx = c;
    }
    public static Context getCtx(){
        return ctx;
    }

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
            HookUtils.installHookMethod("android.app.Application", "attach",
                    Context.class,
                    HookCore.hookCodeForgetContext()
            );

            /*
                验真次数, 修改返回值
             */
            HookUtils.installHookMethod("android.provider.Settings$Secure", "getString",
                    ContentResolver.class,
                    String.class,
                    HookCore.hookCodeForResetAndroidId()
            );

            /*
                自定义水印时间
             */
            HookUtils.installHookConstructorMethod("com.ai.marki.common.util.n1",
                    lpparam.classLoader,
                    long.class, // 第一个参数类型
                    long.class, // 第二个参数类型
                    HookCore.hookCodeForCustomTimeStamp()
                    );

            /*
                替换照片 android.graphics.BitmapFactory.decodeStream.overload("java.io.InputStream").
             */
            HookUtils.installHookMethod("android.graphics.BitmapFactory",
                    "decodeStream",
                    java.io.InputStream.class,
                    HookCore.hookCodeForReplacePic()
                    );


            /*
                设置 翻转
             */
            HookUtils.installHookMethod("android.app.SharedPreferencesImpl",
                    "getBoolean",
                    String.class,
                    boolean.class,
                    HookCore.hookCodeForCcameraMirror()
                    );
        }
    }
}
