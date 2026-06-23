package com.example.server;

import static java.lang.System.out;

import android.util.Log;

import com.example.tool.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientSocketServer {
    public static final String TAG = "ClientForMarki";
    String datetime = "2026-04-01 08:20:00";

    private static Socket socket;
    private DataOutputStream out;
    private DataInputStream inputStream;

    private boolean outPipeState;
    private boolean inputStreamPipeState;

    boolean isInit;

    public boolean getSocketState(){
        return isInit;
    }

    public boolean getComunicationStatus(){
        return outPipeState && inputStreamPipeState;
    }

    public void createSocket() {

        String host = "127.0.0.1";

        int port = 9012;
        socket = null;
        try {
            socket = new Socket(host, port);
            // 设置超时，防止阻塞
            socket.setSoTimeout(3000);

            // 初始化 socket 输入输出
            if (out == null)
                out = new DataOutputStream(socket.getOutputStream());
            if (inputStream == null)
                inputStream = new DataInputStream(socket.getInputStream());
            isInit = true;
            outPipeState = true;
            inputStreamPipeState = true;

            Log.d(TAG, "客户端 socket服务，初始化成功");

        } catch (Exception e) {
            e.printStackTrace();
            isInit = false;
        }
    }

    public void sendMessage(String datetime) throws SocketException {
        try {
            // 发送数据
            String message = Tools.formatTimeToStamp(datetime);
            out.writeInt(message.length());
            out.writeBytes(message);

            // 接收服务端返回的数据（如果需要的话）
            int available = inputStream.available();
            if(available > 0)
            {
                byte[] buf = new byte[available];
                inputStream.read(buf);
                Log.d(TAG, "recv: " + String.valueOf(buf));
            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Broken pipe")){
                outPipeState = false;
                inputStreamPipeState = false;
                try {
                    if (socket != null){
                        socket.close();
                        socket = null;
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
