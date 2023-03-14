package ru.dira.api;

public class RoomMember {


    private String id;

    private String nickname;
    private String imageBase64;
    private String roomSecret;
    private long lastTimeUpdated;

    public RoomMember(String id, String nickname, String imageBase64, String roomSecret, long lastTimeUpdated) {
        this.id = id;
        this.nickname = nickname;
        this.imageBase64 = imageBase64;
        this.roomSecret = roomSecret;
        this.lastTimeUpdated = lastTimeUpdated;
    }

    public long getLastTimeUpdated() {
        return lastTimeUpdated;
    }

    public void setLastTimeUpdated(long lastTimeUpdated) {
        this.lastTimeUpdated = lastTimeUpdated;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
