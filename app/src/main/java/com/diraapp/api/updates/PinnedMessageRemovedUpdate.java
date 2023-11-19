package com.diraapp.api.updates;

public class PinnedMessageRemovedUpdate extends Update {
    private String messageId;

    private String userId;

    public PinnedMessageRemovedUpdate(String roomSecret, String messageId, String userId) {
        super(0, UpdateType.PINNED_MESSAGE_REMOVED_UPDATE);
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
