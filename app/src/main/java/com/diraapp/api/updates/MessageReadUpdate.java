package com.diraapp.api.updates;

public class MessageReadUpdate extends Update {

    private String userId;

    private long readTime;

    private String messageId;

    public MessageReadUpdate(String userId, long readTime, String messageId) {
        super(0, UpdateType.READ_UPDATE);
        this.userId = userId;
        this.readTime = readTime;
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
