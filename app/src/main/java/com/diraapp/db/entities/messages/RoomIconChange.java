package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity
public class RoomIconChange extends CustomClientData {

    private String imagePath;

    @Ignore
    public RoomIconChange(String imagePath) {
        this.setMessageType(MessageType.ROOM_ICON_CHANGE_MESSAGE);
        this.imagePath = imagePath;
    }

    public RoomIconChange() {

    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
