package com.diraapp.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Attachment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fileUrl;
    private long fileCreatedTime;
    private String fileName;
    private long size;
    private AttachmentType attachmentType;

    @Ignore
    private float voiceMessageStopPoint = 0;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public long getFileCreatedTime() {
        return fileCreatedTime;
    }

    public void setFileCreatedTime(long fileCreatedTime) {
        this.fileCreatedTime = fileCreatedTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public float getVoiceMessageStopPoint() {
        return voiceMessageStopPoint;
    }

    public void setVoiceMessageStopProgress(float voiceMessageStopPoint) {
        this.voiceMessageStopPoint = voiceMessageStopPoint;
    }
}
