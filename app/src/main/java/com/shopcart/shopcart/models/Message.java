package com.shopcart.shopcart.models;

import com.google.firebase.firestore.ServerTimestamp;

/**
 * Created by John on 4/2/2018.
 */

public class Message {

    private String message;
    //private long time;
    private boolean seen;
    private String type;
    private String from;
    private String mId;
    private String messageId;

    public Message(){}

    public Message(String message, boolean seen, String type, String from, String mId, String messageId) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.from = from;
        this.mId = mId;
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}
