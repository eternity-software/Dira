package com.diraapp.api.requests.encryption;


import com.diraapp.api.requests.Request;
import com.diraapp.api.requests.RequestType;

public class KeyRenewRequest extends Request {

    private String roomSecret;
    public KeyRenewRequest(String roomSecret) {
        super(0, RequestType.KEY_RENEW_REQUEST);
        this.roomSecret = roomSecret;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }
}
