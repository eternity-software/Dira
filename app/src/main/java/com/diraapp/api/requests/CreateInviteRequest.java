package com.diraapp.api.requests;


import com.diraapp.api.views.RoomMember;
import com.diraapp.db.entities.rooms.RoomType;

import java.util.List;

public class CreateInviteRequest extends Request {

    private String roomName, roomSecret, base64pic;
    private RoomType roomType;
    private List<RoomMember> roomMemberList;

    public CreateInviteRequest(String roomName, String roomSecret, String base64pic,
                               List<RoomMember> roomMemberList, RoomType roomType) {
        super(0, RequestType.CREATE_INVITE);
        this.roomName = roomName;
        this.roomSecret = roomSecret;
        this.base64pic = base64pic;
        this.roomMemberList = roomMemberList;
        this.roomType = roomType;
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

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}
