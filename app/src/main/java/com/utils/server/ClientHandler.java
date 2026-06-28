package com.utils.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.marki.proto.Markiperf;
import com.utils.Tools;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import de.robv.android.xposed.XposedBridge;

public class ClientHandler implements Runnable{
    private final Socket mClientSocket;
    public ClientHandler(Socket client){
        mClientSocket = client;
    }

    @Override
    public void run() {

        int buflen = 0;
        int errCount = 0;
        String clientAddr = this.mClientSocket.getRemoteSocketAddress().toString();
        XposedBridge.log("xposed_module, New client connected: " + clientAddr);

        try{
            DataInputStream dataInputStream = new DataInputStream(this.mClientSocket.getInputStream());
            PrintWriter writer = new PrintWriter(this.mClientSocket.getOutputStream(), true);
            while (errCount < 10) {
                buflen = dataInputStream.readInt();
                XposedBridge.log("xposed_module, buflen: " + buflen);
                byte[] buffer = new byte[buflen];
                dataInputStream.readFully(buffer);
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

    // 解析 protobuf
    private String processContent(byte[] protoBuf) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            Markiperf.MarkiMessage markiMessage = Markiperf.MarkiMessage.parseFrom(protoBuf);
            if (markiMessage.hasPhoto()){
                // 直接设置 图片
                byte[] buf = markiMessage.getPhoto().getData().toByteArray();
                Tools.printHexFromByteArray(buf, 16);  // xposed_module, FF D8 FF E1 0D E1 45 78 69 66 00 00 4D 4D 00 2A
                XposedBridge.log("xposed_module, recv photo size: " + buf.length);
                Tools.setImageBuffer(ByteString.copyFrom(buf).toByteArray());
                stringBuilder.append("替换照片, ");
            }

            if (markiMessage.hasTimeMessage()){
                // 直接设置 修改的时间
                Tools.setTargetTime(markiMessage.getTimeMessage().getTimeStamp());
                stringBuilder.append("修改时间");
            }
            XposedBridge.log("xposed_module, " + stringBuilder.toString());
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log("xposed_module, protobuf 解析异常");

            throw new RuntimeException(e);
        }
        return "pong";
    }
}
