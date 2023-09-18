package com.diraapp.api.views;

public class RoomInfo {

    private String roomSecret;
    private long fromUpdateId;

    public RoomInfo(String roomSecret, long fromUpdateId) {
        this.roomSecret = roomSecret;
        this.fromUpdateId = fromUpdateId;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public long getFromUpdateId() {
        return fromUpdateId;
    }

    public void setFromUpdateId(long fromUpdateId) {
        this.fromUpdateId = fromUpdateId;
    }
}
