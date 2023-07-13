package com.diraapp.db.entities.messages;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;

@Entity
public class RoomJoinClientData extends CustomClientData{

    private String newNickName;

    private String path;

    @Ignore
    public RoomJoinClientData(String newNickName, String path) {
        this.setMessageType(MessageType.NEW_USER_ROOM_JOINING);
        this.newNickName = newNickName;
        this.path = path;
    }

    public RoomJoinClientData() {

    }

    public String getNewNickName() {
        return newNickName;
    }

    public String getPath() {
        return path;
    }
}
