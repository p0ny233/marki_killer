package com.example.server;

import android.util.Log;
import com.example.bean.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import com.example.clientformarki.MainActivity;
import com.google.protobuf.ByteString;
import com.marki.proto.Markiperf;

public class ClientSocketServer{
    public static final String TAG = "ClientForMarki";
    private static Socket socket;
    private DataOutputStream out;
    private DataInputStream inputStream;

    private boolean outPipeState;
    private boolean inputStreamPipeState;
    private LinkedBlockingQueue<Message> mQueue;

    boolean isInit;

    private Callback mCallback;

    public ClientSocketServer(){}

    public  ClientSocketServer(Callback cb, LinkedBlockingQueue<Message> queue){
        this.mQueue = queue;
        this.mCallback = cb;
    }

    public interface Callback {
        void updateLog(String msg);
    }

    public boolean getSocketState(){
        return isInit;
    }

    public boolean getComunicationStatus(){
        return outPipeState && inputStreamPipeState;
    }

    public void createSocket() {

        if (socket != null){
            try {
                if (out != null){
                    out.close();
                }
                if (inputStream != null){
                    inputStream.close();
                }
                socket.close();
            } catch (IOException e) {
                ((MainActivity)this.mCallback).runOnUiThread(
                        ()->{
                            this.mCallback.updateLog("createSocket error, " + e.getMessage());
                        }
                );
                throw new RuntimeException(e);
            }
        }
        isInit = false;
        outPipeState = false;
        inputStreamPipeState = false;
//        String host = "127.0.0.1";
        String host = "192.168.5.8";

        int port = 9012;
        socket = null;
        try {
            // 设置链接超时，防止阻塞
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 2000);

            // 初始化 socket 输入输出
            if (out == null)
                out = new DataOutputStream(socket.getOutputStream());
            if (inputStream == null)
                inputStream = new DataInputStream(socket.getInputStream());
            isInit = true;
            outPipeState = true;
            inputStreamPipeState = true;

            // 可以创建新的线程 循环
            new Thread(){
                @Override
                public void run() {
                    handlerReqQueue();
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "客户端 socket服务，初始化失败, " + e.getMessage());

            if (out != null){
                try {
                    out.close();
                } catch (IOException ex) {
                    Log.d(TAG, "out.close();\n" + ex.getMessage() + "\n");
                    throw new RuntimeException(ex);
                }
            }
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    Log.d(TAG, "inputStream.close();\n" + ex.getMessage() + "\n");
                    throw new RuntimeException(ex);
                }
            }
            isInit = false;
        }
    }

    /*
        监听队列，队列中有消息就处理
     */
    private void handlerReqQueue() {
        try {
            while (true) {
                Markiperf.PhotoMessage.Builder photoMessageBuilder = null;
                Markiperf.TimeMessage.Builder timeMessageBuilder = null;
                com.example.bean.Message data = mQueue.take(); // 队列为空则阻塞，直到有数据
                Markiperf.MarkiMessage.Builder markiMessageBuilder = Markiperf.MarkiMessage.newBuilder();

                // 设置照片
                if (data.getPhotoMessage().getImageBuffer() != null){
                    photoMessageBuilder = Markiperf.PhotoMessage.newBuilder();
                    photoMessageBuilder.setData(ByteString.copyFrom(data.getPhotoMessage().getImageBuffer().toByteArray()));
                    markiMessageBuilder.setPhoto(photoMessageBuilder);

                }
                // 设置时间
                if (data.getTimeMessage().getTimestamp() != null){
                    timeMessageBuilder = Markiperf.TimeMessage.newBuilder();
                    timeMessageBuilder.setTimestamp(data.getTimeMessage().getTimestamp());
                    markiMessageBuilder.setTimeMessage(timeMessageBuilder);
                }
                sendMessage(markiMessageBuilder.build().toByteArray());
                ((MainActivity)this.mCallback).runOnUiThread(
                        ()->{
                            this.mCallback.updateLog("修改成功,请回到水印相机App");
                });
            }

        } catch (SocketException e) {
            Log.d(TAG, "SocketException: " + e.getMessage());
            if (e.getMessage().equals("Broken pipe")){
                outPipeState = false;
                inputStreamPipeState = false;
                isInit = false;
                try {
                    if (socket != null){
                        socket.close();
                        socket = null;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            ((MainActivity)this.mCallback).runOnUiThread(
                    ()->{
                        this.mCallback.updateLog("修改失败, 联系开发者, " + e.getMessage());
            });
        }
    }

    private void sendMessage(byte[] sbuf) throws Exception {
            // 发送数据
            Log.d(TAG, "sbuf.length: " + sbuf.length);
            out.writeInt(sbuf.length);
            out.write(sbuf);
            out.flush();

            // 接收服务端返回的数据（如果需要的话）
            int available = inputStream.available();
            Log.d(TAG, "available: " + available);

            if(available > 0)
            {
                byte[] buf = new byte[available];
                inputStream.read(buf);
                Log.d(TAG, "recv: " + Arrays.toString(buf));
            }
    }
}
