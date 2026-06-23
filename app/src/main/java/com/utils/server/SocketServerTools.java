package com.utils.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.robv.android.xposed.XposedBridge;

public class SocketServerTools extends Thread{

    private ServerSocket mServerSocket;
    private int mPort;

    private boolean isRunning = true;
    private ExecutorService threadPool;

    @Override
    public void run() {
        try {
            // 监听一个随机空闲端口 (0 表示系统自动分配)
            mServerSocket = new ServerSocket(9012);
            mPort = mServerSocket.getLocalPort();
            // 可以在这里打印端口号，方便客户端连接
            XposedBridge.log("xposed_module, mPort: " + mPort);
            // 创建缓存线程池，处理大量短连接或长连接
            threadPool = Executors.newCachedThreadPool();
            while (isRunning && !mServerSocket.isClosed()) {
                try {
                    Socket client = mServerSocket.accept();
                    // 每来一个客户端，丢给线程池处理
                    threadPool.execute(new ClientHandler(client));
                } catch (Exception e) {
                    if (isRunning) {
                        XposedBridge.log("xposed_module, Accept error, " +  e.getMessage());
                        mServerSocket.close();
                    }
                }
            }
        } catch (IOException e) {
            XposedBridge.log("xposed_module, Server init error, " +  e.getMessage());
        }
    }
}
