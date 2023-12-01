package com.diraapp.api.views;


import com.diraapp.db.entities.rooms.RoomType;

import java.util.ArrayList;
import java.util.List;

public class InviteRoom {

    private String secretName;

    private String name;
    private String base64pic;

    private RoomType roomType;
    private List<RoomMember> roomMemberList = new ArrayList<>();

    public InviteRoom(String name, String secretName, String base64pic,
                      List<RoomMember> roomMemberList, RoomType roomType) {
        this.name = name;
        this.base64pic = base64pic;
        this.roomMemberList = roomMemberList;
        this.secretName = secretName;
        this.roomType = roomType;
    }

    public List<RoomMember> getMemberList() {
        return roomMemberList;
    }

    public void setMemberList(List<RoomMember> memberList) {
        this.roomMemberList = memberList;
    }

    public String getBase64pic() {
        return base64pic;
    }

    public void setBase64pic(String base64pic) {
        this.base64pic = base64pic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}
