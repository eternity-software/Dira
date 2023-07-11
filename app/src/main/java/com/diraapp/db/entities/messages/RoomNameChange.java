package com.diraapp.db.entities.messages;

import androidx.room.Entity;

@Entity
public class RoomNameChange extends CustomClientData {

    private String newName;

    private String oldName;

    public RoomNameChange(String newName, String oldName) {
        this.setMessageType(MessageType.ROOM_NAME_CHANGE_MESSAGE);
        this.newName = newName;
        this.oldName = oldName;
    }

    public RoomNameChange() {
        super();
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

}
