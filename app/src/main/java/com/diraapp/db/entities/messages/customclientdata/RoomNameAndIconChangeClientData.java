package com.diraapp.db.entities.messages.customclientdata;

import androidx.room.Entity;

import com.diraapp.db.entities.messages.MessageType;

@Entity
public class RoomNameAndIconChangeClientData extends CustomClientData {

    private String newName;

    private String oldName;

    private String path;

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
}
