package com.diraapp.api.requests;

public class VerifyRoomInfoRequest extends Request {

    private String name, roomSecret;


    public VerifyRoomInfoRequest(String name, String roomSecret) {
        super(0, RequestType.VERIFY_ROOM_INFO);
        this.name = name;
        this.roomSecret = roomSecret;
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
