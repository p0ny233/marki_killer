package com.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.XposedBridge;

public class Tools {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static Context ctx;

    private static SharedPreferences sp;

    public static class Inner{
        public Inner(Context context){
            if (ctx == null)
                ctx = context;
            if (sp == null){
                sp = ctx.getSharedPreferences("mark_crack_time", Context.MODE_PRIVATE);
                sp.edit().clear().apply();
            }
        }
    }
    // 从 本地文件 中获取时间
    public static long getTargetTime() {
        /*
            从 SharedPrefrence读取time值
            首先要获取 context 实例
            https://xrefandroid.com/android-11.0.0_r48/xref/frameworks/base/core/java/android/app/Application.java#350
         */
//        SharedPreferences sp = ctx.getSharedPreferences("marki_crack_info", Context.MODE_PRIVATE);
        long t = sp.getLong("target_time", 0);
        XposedBridge.log("t: " + t);

        /*

        <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
        <map>
            <long name="target_time" value="1777594800000" />
        </map>

            1777594800000   -> 2026-05-01 08:20:00
            1781569440000   -> 2026-06-16 08:24:00
         */
        return t;
    }


    public static void setTargetTime(long time) {
        /*
            从 SharedPrefrence读取time值
            首先要获取 context 实例
            https://xrefandroid.com/android-11.0.0_r48/xref/frameworks/base/core/java/android/app/Application.java#350
         */
        XposedBridge.log("setTargetTime");
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("target_time", time);
        editor.apply();
        XposedBridge.log("set target_time: " + time);

        /*

        <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
        <map>
            <long name="target_time" value="1777594800000" />
        </map>
         */
    }

    public static String formatTime(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /*
        随机生成 Android id
     */

    // 定义允许出现的字符（可根据需要增删）

    public static String generateAndroid_ID(int length) {

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }
}
;