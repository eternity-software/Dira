package com.diraapp.api.requests;

import com.diraapp.api.views.BaseMember;


/**
 * Member reaction for PingUpdate
 */
public class PingReactRequest extends Request{

    private String roomSecret;
    private BaseMember baseMember;

    public PingReactRequest(String roomSecret, BaseMember baseMember) {
        super(0, RequestType.PING_REACT);
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
