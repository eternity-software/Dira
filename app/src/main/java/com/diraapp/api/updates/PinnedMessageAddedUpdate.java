package com.diraapp.api.updates;

import com.diraapp.api.requests.RequestType;

public class PinnedMessageAddedUpdate extends Update {

    private String messageId;

    private String userId;

    public PinnedMessageAddedUpdate(String roomSecret, String messageId, String userId) {
        super(0, UpdateType.PINNED_MESSAGE_ADDED_UPDATE);
        this.messageId = messageId;
        this.userId = userId;
        this.setRoomSecret(roomSecret);
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
