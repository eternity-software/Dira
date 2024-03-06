package com.diraapp.storage;

public class AttachmentInfo {

    private final long id;

    private final String messageId;

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
