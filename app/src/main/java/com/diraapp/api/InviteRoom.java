package com.diraapp.api;


import java.util.ArrayList;
import java.util.List;

public class InviteRoom {

    private String secretName;

    private String name;
    private String base64pic;

    private List<RoomMember> roomMemberList = new ArrayList<>();

    public InviteRoom(String name, String secretName, String base64pic, List<RoomMember> roomMemberList) {
        this.name = name;
        this.base64pic = base64pic;
        this.roomMemberList = roomMemberList;
        this.secretName = secretName;
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
}
