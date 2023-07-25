package com.diraapp.api.requests.encryption;

import com.diraapp.api.requests.Request;
import com.diraapp.api.requests.RequestType;
import com.diraapp.api.views.DhKey;


public class SendIntermediateKey extends Request {
    private DhKey dhKey;

    private String roomSecret;

    public SendIntermediateKey(DhKey dhKey, String roomSecret) {
        super(0, RequestType.SEND_INTERMEDIATE_KEY);
        this.dhKey = dhKey;
        this.roomSecret = roomSecret;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public DhKey getDhKey() {
        return dhKey;
    }

    public void setDhKey(DhKey dhKey) {
        this.dhKey = dhKey;
    }
}
