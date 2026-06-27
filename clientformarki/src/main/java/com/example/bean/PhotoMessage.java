package com.example.bean;

import java.io.ByteArrayOutputStream;

public class PhotoMessage {

    private ByteArrayOutputStream ImageBuffer;

    private long fileSize;

    private String FileMd5;
    public void setImageBuffer(byte[] buffer) {
        if (buffer == null || buffer.length == 0){
            ImageBuffer = null;
            fileSize = 0;
            return;
        }
        this.ImageBuffer = new ByteArrayOutputStream();
        this.ImageBuffer.write(buffer, 0, buffer.length);
        fileSize = buffer.length;
    }
    public ByteArrayOutputStream getImageBuffer() {
        return this.ImageBuffer;
    }

    public long getfileSize() {
        return fileSize;
    }

    public String getFileMd5() {
        return FileMd5;
    }

    public void setfileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileMd5(String FileMd5) {
        this.FileMd5 = FileMd5;
    }
}
