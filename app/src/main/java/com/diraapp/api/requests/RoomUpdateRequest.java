package com.diraapp.api.requests;

public class RoomUpdateRequest extends Request {

    private String base64Picture;
    private String name;
    private String roomSecret;
    private int updateExpireSec;

    public RoomUpdateRequest(String base64Picture, String name, String roomSecret, int updateExpireSec) {
        super(0, RequestType.UPDATE_ROOM);
        this.base64Picture = base64Picture;
        this.name = name;
        this.roomSecret = roomSecret;
        this.updateExpireSec = updateExpireSec;
    }

    public int getUpdateExpireSec() {
        return updateExpireSec;
    }

    public void setUpdateExpireSec(int updateExpireSec) {
        this.updateExpireSec = updateExpireSec;
    }

    public String getBase64Picture() {
        return base64Picture;
    }

    public void setBase64Picture(String base64Picture) {
        this.base64Picture = base64Picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }
}
