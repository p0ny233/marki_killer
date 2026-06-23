package com.utils.server;

import com.utils.Tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import de.robv.android.xposed.XposedBridge;

public class ClientHandler implements Runnable{
    private final Socket mClientSocket;
    public ClientHandler(Socket client){
        mClientSocket = client;
    }

    @Override
    public void run() {

        int buflen = 0;
        byte[] buf;
        int errCount = 0;
        String clientAddr = this.mClientSocket.getRemoteSocketAddress().toString();
        XposedBridge.log("xposed_module, New client connected: " + clientAddr);
        try{
            DataInputStream dataInputStream = new DataInputStream(this.mClientSocket.getInputStream());
            PrintWriter writer = new PrintWriter(this.mClientSocket.getOutputStream(), true);
            while (errCount < 10) {
                int length = dataInputStream.readInt();
                XposedBridge.log("length: " + length);

                if (length > 13)
                {
                    // 复位
                    length = dataInputStream.available();
                    dataInputStream.read(new byte[length]);
                    XposedBridge.log("错误指令, 已清空缓冲区, 重新接收指令");
                    continue;
                }
                byte[] buffer = new byte[length];
                dataInputStream.readFully(buffer);
                XposedBridge.log("xposed_module, Received: " + buffer);
                String response = processContent(buffer);
                writer.println(response);
            }
        } catch (Exception e) {
            errCount++;
            XposedBridge.log("xposed_module, Client handler error, " + e.getMessage());
        } finally {
            try {
                if (this.mClientSocket != null) this.mClientSocket.close();
            } catch (Exception ignored) {}
            XposedBridge.log("xposed_module, Client disconnected: " + clientAddr);
        }
    }

    // 示例指令处理（可替换为你的Hook交互逻辑）
    private String processContent(byte[] content) {
        // 写入 目标时间 到 SharedPrefrence 存储
        String str = new String(content);
        XposedBridge.log("xposed_module, content: " + str);
        XposedBridge.log("hello");
        Long t = Long.valueOf(str);
        XposedBridge.log("Long.valueOf: " + t);
        Tools.setTargetTime(t);
        return "pong";
    }
}
