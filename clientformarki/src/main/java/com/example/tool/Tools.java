package com.example.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

    /*
        字符串类型的"2026-05-01 08:20" 转化为 字符串类型的"1777594800000"
     */
    public static String formatTimeToStamp(String formatTime){
        /*
            格式化的时间转为 时间戳字符串
         */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            long timestamp = sdf.parse(formatTime).getTime();
            return String.valueOf(timestamp);

        } catch (ParseException e) {
//            throw new RuntimeException(e);
            return null;
        }
    }

    /*
        long类型的时间戳 转为 字符串类型的日期"2020-01-01: 01:01:00"
     */
    public static String formatTime(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public static byte[] readRawBytesFromInputStream(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  // ByteArrayOutputStream 拼接内存用
        byte[] buffer = new byte[8192];
        int byteReads = -1;
        try{

            while ((byteReads = is.read(buffer))!= -1){
                baos.write(buffer, 0, byteReads);
            return baos.toByteArray();
            }
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
