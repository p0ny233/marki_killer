package com.example.bean;

public class Message {

    private PhotoMessage photoMessage;
    private TimeMessage timeMessage;

    public Message(){}

    public void setPhotoMessage(PhotoMessage photoMessage) {
        this.photoMessage = photoMessage;
    }
    public void setTimeMessage(TimeMessage timeMessage) {
        this.timeMessage = timeMessage;
    }

    public PhotoMessage getPhotoMessage() {
        return photoMessage;
    }

    public TimeMessage getTimeMessage() {
        return timeMessage;
    }
}
