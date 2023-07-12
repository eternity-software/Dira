package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity
public class RoomJoinClientData extends CustomClientData{

    private String UserNickName;

    @Ignore
    public RoomJoinClientData(String userNickName) {
        this.setMessageType(MessageType.NEW_USER_ROOM_JOINING);
        this.UserNickName = userNickName;
    }

    public RoomJoinClientData() {

    }

    public String getUserNickName() {
        return UserNickName;
    }

    public void setUserNickName(String userNickName) {
        UserNickName = userNickName;
    }
}
