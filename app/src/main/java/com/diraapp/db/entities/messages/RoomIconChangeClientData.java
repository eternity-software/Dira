package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity
public class RoomIconChangeClientData extends CustomClientData {

    private String imagePath;

    @Ignore
    public RoomIconChangeClientData(String imagePath) {
        this.setMessageType(MessageType.ROOM_ICON_CHANGE_MESSAGE);
        this.imagePath = imagePath;
    }

    public RoomIconChangeClientData() {

    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
