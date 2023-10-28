package com.diraapp.storage.attachments;

import android.content.Context;

import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.AttachmentDownloadHandler;

public class SaveAttachmentTask {
    private Context context;
    private boolean isSizeLimited;

    private Attachment attachment;
    private String roomSecret;

    private AttachmentDownloadHandler attachmentDownloadHandler;

    public SaveAttachmentTask(Context context, boolean isSizeLimited, Attachment attachment, String roomSecret) {
        this.context = context;
        this.isSizeLimited = isSizeLimited;

        this.attachment = attachment;
        this.roomSecret = roomSecret;
    }

    public AttachmentDownloadHandler getAttachmentDownloadHandler() {
        return attachmentDownloadHandler;
    }

    public void setAttachmentDownloadHandler(AttachmentDownloadHandler attachmentDownloadHandler) {
        this.attachmentDownloadHandler = attachmentDownloadHandler;
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

    public boolean isSizeLimited() {
        return isSizeLimited;
    }

    public void setSizeLimited(boolean sizeLimited) {
        isSizeLimited = sizeLimited;
    }


    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
