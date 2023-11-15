package com.diraapp.api.requests;

public class AttachmentListenedRequest extends Request {

    private String messageId;

    private String userId;

    public AttachmentListenedRequest(String roomSecret, String messageId, String memberId) {
        super(0, RequestType.ATTACHMENT_LISTENED_REQUEST);
        this.messageId = messageId;
        this.userId = memberId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
