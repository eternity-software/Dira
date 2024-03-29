package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

@Entity
public class RoomJoinClientData extends CustomClientData {

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

    @Override
    public String getText(Context context) {
        return context.getString(R.string.room_update_new_member)
                .replace("%s", newNickName);
    }
}
