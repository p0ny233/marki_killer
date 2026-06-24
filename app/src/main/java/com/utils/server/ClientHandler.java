package com.utils.server;

import com.utils.Tools;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

public class ClientHandler implements Runnable{
    private final Socket mClientSocket;
    public ClientHandler(Socket client){
        mClientSocket = client;
    }

    @Override
    public void run() {

        int buflen = 0;
        int insType;     // 指令类型

        int errCount = 0;
        String clientAddr = this.mClientSocket.getRemoteSocketAddress().toString();
        XposedBridge.log("xposed_module, New client connected: " + clientAddr);

        /*
            协议格式: [4个字节的指令类型][4个字节表示data的数据长度][data]
         */
        try{
            DataInputStream dataInputStream = new DataInputStream(this.mClientSocket.getInputStream());
            PrintWriter writer = new PrintWriter(this.mClientSocket.getOutputStream(), true);
            while (errCount < 10) {
                insType = dataInputStream.readInt();
                XposedBridge.log("insType: " + insType);

                buflen = dataInputStream.readInt();
                XposedBridge.log("buflen: " + buflen);

                // 复位
                buflen = dataInputStream.available();

                byte[] buffer = new byte[buflen];
                dataInputStream.readFully(buffer);
                XposedBridge.log("xposed_module, Received: " + buffer);
                String response = processContent(insType, buffer);
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
    private String processContent(int insType, byte[] content) {

        switch (insType){
            case 1:  // 修改 马克时间的类型
                // 写入 目标时间 到 SharedPrefrence 存储
                String str = new String(content);
                XposedBridge.log("xposed_module, content: " + str);

                Long t = Long.valueOf(str);
                XposedBridge.log("xposed_module, Long.valueOf: " + t);
                Tools.setTargetTime(t);
                break;
            case 2:  // 修改 替换照片类型
                // 是否启用 开关
                ByteBuffer buf = ByteBuffer.wrap(content);
                int switchFlag = buf.getInt();
                XposedBridge.log("xposed_module, switchFlag: " + switchFlag);
                if (1 == switchFlag){
                    // 解析图片路径
                    String path = new String(buf.array()).substring(4);
                    XposedBridge.log("xposed_module, path: " + path);
                    Tools.switchReplace(true,path);
                }
                else
                    Tools.switchReplace(false, "");
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
        }
        return "pong";
    }
}
