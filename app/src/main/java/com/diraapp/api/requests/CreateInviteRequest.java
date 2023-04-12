package com.diraapp.api.requests;


import com.diraapp.api.RoomMember;

import java.util.List;

public class CreateInviteRequest extends Request {

    private String roomName, roomSecret, base64pic;
    private List<RoomMember> roomMemberList;

    public CreateInviteRequest(String roomName, String roomSecret, String base64pic, List<RoomMember> roomMemberList) {
        super(0, RequestType.CREATE_INVITE);
        this.roomName = roomName;
        this.roomSecret = roomSecret;
        this.base64pic = base64pic;
        this.roomMemberList = roomMemberList;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public String getBase64pic() {
        return base64pic;
    }

    public void setBase64pic(String base64pic) {
        this.base64pic = base64pic;
    }

    public List<RoomMember> getMemberList() {
        return roomMemberList;
    }

    public void setMemberList(List<RoomMember> roomMemberList) {
        this.roomMemberList = roomMemberList;
    }
}
