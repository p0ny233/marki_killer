package com.example.tool;
import android.text.format.DateFormat;

import java.util.Date;

public class LogUtils {
    public static String debug(String msg){
        String time = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date()).toString();

        return new String(time + " [DEBUG] " + msg + "\n");

    }
}
