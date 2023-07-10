package com.diraapp.db.entities.messages;

import androidx.room.Entity;

@Entity
public class RoomNameChange extends CustomClientData {

    private String newName;

    private String oldName;

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
