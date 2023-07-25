package com.diraapp.api.requests.encryption;


import com.diraapp.api.requests.Request;
import com.diraapp.api.requests.RequestType;
import com.diraapp.api.views.BaseMember;

public class SubmitKeyRequest extends Request {

    private String roomSecret;
    private BaseMember baseMember;

    public SubmitKeyRequest(String roomSecret, BaseMember baseMember) {
        super(0, RequestType.SUBMIT_KEY);
        this.roomSecret = roomSecret;
        this.baseMember = baseMember;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public BaseMember getBaseMember() {
        return baseMember;
    }

    public void setBaseMember(BaseMember baseMember) {
        this.baseMember = baseMember;
    }
}
