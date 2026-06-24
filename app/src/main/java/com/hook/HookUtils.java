package com.hook;

import de.robv.android.xposed.XposedHelpers;

public class HookUtils {

    public static void installHookMethod(String className, String methodName, Object... parameterTypesAndCallback){
        try {
            XposedHelpers.findAndHookMethod(
                    Class.forName(className),  // 返回 指定类的元数据
                    methodName,
                    parameterTypesAndCallback
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void installHookConstructorMethod(String className, ClassLoader classLoader, Object... parameterTypesAndCallback){
            XposedHelpers.findAndHookConstructor(
                    className,  // String类型，而不是 Class对象，否则会抛出 ClassNotFoundException
                    classLoader,
                    parameterTypesAndCallback
            );

    }
}
