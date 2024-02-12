package com.diraapp.storage;

public class AttachmentInfo {

    private long id;

    private String messageId;

    public AttachmentInfo(long id, String messageId) {
        this.id = id;
        this.messageId = messageId;
    }

    public long getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }
}
