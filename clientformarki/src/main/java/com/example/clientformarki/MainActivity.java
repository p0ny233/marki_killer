package com.example.clientformarki;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bean.Message;
import com.example.bean.PhotoMessage;
import com.example.bean.TimeMessage;
import com.example.server.ClientSocketServer;
import com.example.tool.LogUtils;
import com.example.tool.Tools;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.io.FileNotFoundException;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity implements ClientSocketServer.Callback{

    static {
       System.loadLibrary("clientformarki");
    }

    private static final String TAG = "ClientForMarki";
    private int CHOOSE_CODE = 3;

    // 按钮
    private Button dateBtn;
    private FrameLayout frameLayoutBtn;
    private Button initBtn;
    private Button clearLogBtn;
    private Button cancelBtn;
    private Button confirmBtn;
    private ShapeableImageView preview_img;
    private MaterialTimePicker timePicker;
    private EditText dateEd;
    private EditText logEd;
    private String dateTime;
    // imageUri
    private Uri imgUri;

    private ClientSocketServer mClientSocketServer;

    private LinkedBlockingQueue<Message> mQueue =  new LinkedBlockingQueue<Message>();

    private void initGui(){
        // 初始化控件
        dateBtn = findViewById(R.id.dateBtn);
        dateEd = findViewById(R.id.dateEd);
        initBtn = findViewById(R.id.initBtn);
        confirmBtn = findViewById(R.id.confirmBtn);
        preview_img = findViewById(R.id.preview_img);  // 展示图片控件
        logEd = findViewById(R.id.logEd);
        clearLogBtn = findViewById(R.id.clearLogBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        confirmBtn.setEnabled(false);

        // 图片预览区
        frameLayoutBtn = findViewById(R.id.pic_select);
        // 创建 GradientDrawable
        GradientDrawable drawable = new GradientDrawable();
        // 设置边框（宽度，颜色）
        drawable.setStroke(3, Color.GRAY);
        // 设置背景透明
        drawable.setColor(Color.TRANSPARENT);
        frameLayoutBtn.setBackground(drawable);

        // 建立通信
        initBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        if(mClientSocketServer == null)
                            mClientSocketServer = new ClientSocketServer(MainActivity.this, mQueue);

                        if (!mClientSocketServer.getSocketState() || !mClientSocketServer.getComunicationStatus())
                            mClientSocketServer.createSocket();

                        if (mClientSocketServer.getSocketState() && mClientSocketServer.getComunicationStatus()){
                            runOnUiThread(()->{
                                logEd.append(LogUtils.debug("初始化服务成功"));
                                confirmBtn.setEnabled(true);
                            });
                        }else {
                            runOnUiThread(()->{
                                logEd.append(LogUtils.debug("初始化服务成失败"));
                            });
                        }
                    }
                }.start();
            }
        });

        // 点击按钮获取当前选中的时间
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 创建一个 datepickerDialog
                MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select dates")
                        .build();

                datePicker.show(getSupportFragmentManager(), "TAG");

                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        dateTime = Tools.formatTime(Long.valueOf(selection.toString()));
                        timePicker.show(getSupportFragmentManager(), "TAG");
                    }
                });

                // 时间
                timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(7)
                        .setMinute(50)
                        .setTitleText("选择时间")
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build();
                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 点击确定
                        Log.d(TAG, "addOnPositiveButtonClickListener");
                        String time = new String(String.format(" %02d:%02d", timePicker.getHour(), timePicker.getMinute()));
                        dateTime += time;
                        dateEd.setText(dateTime);
                    }
                });
            }
        });

        // 点击选择图片
        frameLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出进入相册的窗口
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, CHOOSE_CODE);
            }
        });

        clearLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logEd.setText("");
            }
        });

        /*
            取消预览的图片
         */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgUri = null;
                preview_img.setImageDrawable(null);
            }
        });

        /*
            确认修改，根据配置信息，封装 Message类
         */
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean m;
                if (!mClientSocketServer.getSocketState() || !mClientSocketServer.getComunicationStatus()){
                    logEd.append(LogUtils.debug("修改失败，请先重新初始化"));
                }

                TimeMessage timeMessage = new TimeMessage();
                PhotoMessage photoMessage = new PhotoMessage();
                Message message = new Message();

                message.setPhotoMessage(photoMessage);
                message.setTimeMessage(timeMessage);

                long timeStamp = Tools.formatTimeToStamp(dateEd.getText().toString());
                timeMessage.setTimestamp(timeStamp);

                if (timeStamp != 0)
                    m = true;

                byte[] buffer = null;
                try {
                    if (imgUri != null){
                        buffer = Tools.readRawBytesFromInputStream(getContentResolver().openInputStream(imgUri));
                        m = true;
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (NullPointerException e){
                }
                photoMessage.setImageBuffer(buffer);
                mQueue.offer(message);
            }
        });
    } // initGui end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGui();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_CODE && resultCode==RESULT_OK){  // 从相册回来
            if (data.getData() != null){
                imgUri = data.getData(); // 获得已选择照片的路径对象
                preview_img.setImageURI(imgUri);
            }
        }
    }

    // 回调函数，作为其它类的cb
    public void updateLog(String msg){
        logEd.append(LogUtils.debug(msg));
    }

}
