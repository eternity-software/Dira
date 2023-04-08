package com.diraapp.api.updates;

public class Update {

    private long originRequestId = -1;
    private long updateId = 0;
    private long updateExpireSec = 8 * 60 * 60;
    private final long updateCreatedTime;
    private String roomSecret;
    private final UpdateType updateType;

    public Update(long updateId, UpdateType updateType) {
        this.updateId = updateId;
        this.updateType = updateType;
        updateCreatedTime = System.currentTimeMillis();
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public long getOriginRequestId() {
        return originRequestId;
    }

    public Update setOriginRequestId(long originRequestId) {
        this.originRequestId = originRequestId;
        return this;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public long getUpdateExpireSec() {
        return updateExpireSec;
    }

    public void setUpdateExpireSec(long updateExpireSec) {
        this.updateExpireSec = updateExpireSec;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - updateCreatedTime > updateExpireSec * 1000;
    }

    public long getUpdateId() {
        return updateId;
    }

    public void setUpdateId(long updateId) {
        this.updateId = updateId;
    }
}
