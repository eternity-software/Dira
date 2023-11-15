package com.diraapp.api.updates;

public class AttachmentListenedUpdate extends Update {

    private String userId;

    private String messageId;

    public AttachmentListenedUpdate(String roomSecret, String messageId, String memberId) {
        super(0, UpdateType.ATTACHMENT_LISTENED_UPDATE);
        this.userId = memberId;
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
