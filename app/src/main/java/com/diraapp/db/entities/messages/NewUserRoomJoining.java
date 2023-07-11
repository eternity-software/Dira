package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity
public class NewUserRoomJoining extends CustomClientData{

    private String UserNickName;

    @Ignore
    public NewUserRoomJoining(String userNickName) {
        this.setMessageType(MessageType.NEW_USER_ROOM_JOINING);
        this.UserNickName = userNickName;
    }

    public NewUserRoomJoining() {

    }

    public String getUserNickName() {
        return UserNickName;
    }

    public void setUserNickName(String userNickName) {
        UserNickName = userNickName;
    }
}
