package com.diraapp.ui.adapters.messages.views.viewholders.groups;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Room;


public class AttachmentItem  {

    private Attachment attachment;
    private Room room;

    public AttachmentItem(Attachment attachment, Room room) {
        this.attachment = attachment;
        this.room = room;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }



}
