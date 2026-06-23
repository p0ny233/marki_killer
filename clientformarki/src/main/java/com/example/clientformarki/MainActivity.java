package com.example.clientformarki;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.server.ClientSocketServer;

import java.net.SocketException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ClientForMarki";
    private NumberPicker pickerYear;
    private NumberPicker pickerMonth;
    private NumberPicker pickerDay;
    private NumberPicker pickerHour;
    private NumberPicker pickerMinute;
    private Button btnGetTime;
    private Button btnInitServer;
    private Button btnCheatTime;
    private TextView tvResult;

    private static ClientSocketServer mClientSocketServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        pickerYear   = findViewById(R.id.pickerYear);
        pickerMonth  = findViewById(R.id.pickerMonth);
        pickerDay    = findViewById(R.id.pickerDay);
        pickerHour   = findViewById(R.id.pickerHour);
        pickerMinute = findViewById(R.id.pickerMinute);
        btnGetTime   = findViewById(R.id.btnGetTime);
        tvResult     = findViewById(R.id.tvResult);
        btnInitServer = findViewById(R.id.btnInitServer);
        btnCheatTime = findViewById(R.id.btnCheatTime);


        // 用当前时间初始化各选择器
        Calendar now = Calendar.getInstance();
        int currentYear   = now.get(Calendar.YEAR);
        int currentMonth  = now.get(Calendar.MONTH) + 1; // Calendar.MONTH 从 0 开始
        int currentDay    = now.get(Calendar.DAY_OF_MONTH);
        int currentHour   = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // 年：当前年前后 10 年
        pickerYear.setMinValue(currentYear - 10);
        pickerYear.setMaxValue(currentYear + 10);
        pickerYear.setValue(currentYear);
        pickerYear.setWrapSelectorWheel(false);

        // 月：1 ~ 12
        pickerMonth.setMinValue(1);
        pickerMonth.setMaxValue(12);
        pickerMonth.setValue(currentMonth);
        pickerMonth.setWrapSelectorWheel(true);
        // 显示两位数补零
        pickerMonth.setDisplayedValues(new String[]{
                "01","02","03","04","05","06","07","08","09","10","11","12"
        });

        // 日：先初始化为 1~31，随月份变化动态调整
        pickerDay.setMinValue(1);
        pickerDay.setMaxValue(getDaysInMonth(currentYear, currentMonth));
        pickerDay.setValue(currentDay);
        pickerDay.setWrapSelectorWheel(true);

        // 时：0 ~ 23
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        pickerHour.setValue(currentHour);
        pickerHour.setWrapSelectorWheel(true);
        pickerHour.setDisplayedValues(generateTwoDigitStrings(0, 23));

        // 分：0 ~ 59
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setValue(currentMinute);
        pickerMinute.setWrapSelectorWheel(true);
        pickerMinute.setDisplayedValues(generateTwoDigitStrings(0, 59));

        // 年或月变化时，动态更新日的最大值
        NumberPicker.OnValueChangeListener updateDayRange = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int year  = pickerYear.getValue();
                int month = pickerMonth.getValue();
                int maxDay = getDaysInMonth(year, month);
                // 必须先设 max 再刷新，避免当前值超出范围
                if (pickerDay.getValue() > maxDay) {
                    pickerDay.setValue(maxDay);
                }
                pickerDay.setMaxValue(maxDay);
            }
        };
        pickerYear.setOnValueChangedListener(updateDayRange);
        pickerMonth.setOnValueChangedListener(updateDayRange);

        // 点击按钮获取当前选中的时间
        btnGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year   = pickerYear.getValue();
                int month  = pickerMonth.getValue();
                int day    = pickerDay.getValue();
                int hour   = pickerHour.getValue();
                int minute = pickerMinute.getValue();

                // 秒取设备当前秒数
                int second = Calendar.getInstance().get(Calendar.SECOND);

                String result = String.format(
                        "%d-%02d-%02d %02d:%02d:%02d",  // 2026-04-01 08:20:00
                        year, month, day, hour, minute, second
                );
                tvResult.setText(result);
            }
        });


        // 初始化按钮
        btnInitServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        init_cheat();
                    }
                });
                try {
                    t.start();
                    t.join(3000);
                    if (mClientSocketServer.getSocketState() && mClientSocketServer.getComunicationStatus())
                        Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                    else{
                        Toast.makeText(MainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                        mClientSocketServer = null;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        // 发送时间按钮
        btnCheatTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mClientSocketServer == null || !mClientSocketServer.getSocketState()){
                    Toast.makeText(MainActivity.this, "先点击初始化服务,才能进行后续操作",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!mClientSocketServer.getComunicationStatus()){
                    Toast.makeText(MainActivity.this, "与马克水印相机通信失败，" +
                                    "确保马克水印相机已在运行，然后重新点击初始化按钮",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String time = tvResult.getText().toString();
                if (!time.startsWith("2"))
                {
                    Toast.makeText(MainActivity.this,
                            "先选择日期，再点击【修改时间】按钮",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cheatForTime();
                    }
                }).start();

                Toast.makeText(MainActivity.this,
                        "成功修改时间，请重新到水印App",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }// onCreate end

    private void init_cheat(){
        if (mClientSocketServer == null)
            mClientSocketServer = new ClientSocketServer();
        if (!mClientSocketServer.getSocketState())
            mClientSocketServer.createSocket();
    }

    private void cheatForTime(){
        String time = tvResult.getText().toString();
        try {
            mClientSocketServer.sendMessage(time);
        }catch (SocketException e){
            if (e.getMessage().equals("Broken pipe")){
                mClientSocketServer = null;
            }
        }
    }

    /**
     * 返回指定年月的天数（正确处理闰年 2 月）
     */
    private int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar.MONTH 从 0 开始
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 生成从 start 到 end 的两位数补零字符串数组
     * 例如：0 -> "00"，5 -> "05"，23 -> "23"
     */
    private String[] generateTwoDigitStrings(int start, int end) {
        String[] result = new String[end - start + 1];
        for (int i = start; i <= end; i++) {
            result[i - start] = String.format("%02d", i);
        }
        return result;
    }
}
