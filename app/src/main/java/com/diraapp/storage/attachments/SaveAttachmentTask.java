package com.diraapp.storage.attachments;

import android.content.Context;

import com.diraapp.db.entities.Attachment;

public class SaveAttachmentTask {
    private Context context;
    private boolean isAutoLoad;

    private Attachment attachment;
    private String roomSecret;

    public SaveAttachmentTask(Context context, boolean isAutoLoad, Attachment attachment, String roomSecret) {
        this.context = context;
        this.isAutoLoad = isAutoLoad;

        this.attachment = attachment;
        this.roomSecret = roomSecret;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isAutoLoad() {
        return isAutoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        isAutoLoad = autoLoad;
    }


    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
