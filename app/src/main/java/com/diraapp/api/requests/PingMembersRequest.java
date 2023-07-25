package com.diraapp.api.requests;

/**
 * Request from client to get all online members
 */
public class PingMembersRequest extends Request{

    private String roomSecret;
    public PingMembersRequest(String roomSecret) {
        super(0, RequestType.PING_MEMBERS);
        this.roomSecret = roomSecret;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }
}
