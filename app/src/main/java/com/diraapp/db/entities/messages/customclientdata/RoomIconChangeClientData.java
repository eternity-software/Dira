package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

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

    @Override
    public String getText(Context context) {
        return context.getString(R.string.room_update_picture_change);
    }
}
