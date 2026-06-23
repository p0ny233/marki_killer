package com.example.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Tools {

    public static String formatTimeToStamp(String formatTime){
        /*
            格式化的时间转为 时间戳字符串
         */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long timestamp = sdf.parse(formatTime).getTime();
            return String.valueOf(timestamp);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }
}
