package com.diraapp.db.entities.messages;

import androidx.room.Entity;

@Entity
public class RoomIconChange extends CustomClientData {

    private String imagePath;

    public RoomIconChange() {

    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
