package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import androidx.room.Entity;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

@Entity
public class RoomNameAndIconChangeClientData extends CustomClientData {

    private final String newName;

    private final String oldName;

    private final String path;

    public RoomNameAndIconChangeClientData(String newName, String oldName, String path) {
        this.setMessageType(MessageType.ROOM_NAME_AND_ICON_CHANGE_MESSAGE);
        this.newName = newName;
        this.oldName = oldName;
        this.path = path;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldName() {
        return oldName;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.room_update_name_and_picture_change);
    }
}
