package com.diraapp.db.entities;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.diraapp.storage.AppStorage;

@Entity
public class Attachment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fileUrl;
    private long fileCreatedTime;
    private String fileName;
    private long size;
    private AttachmentType attachmentType;

    @ColumnInfo(defaultValue = "-1")
    private int height;

    @ColumnInfo(defaultValue = "-1")
    private int width;

    @ColumnInfo(defaultValue = "")
    private String imagePreview;

    @Ignore
    private float voiceMessageStopPoint = 0;

    public String getImagePreview() {
        return imagePreview;
    }

    public void setImagePreview(String imagePreview) {
        this.imagePreview = imagePreview;
    }

    public Bitmap getBitmapPreview() {
        try {
            return Bitmap.createScaledBitmap(AppStorage.getBitmapFromBase64(imagePreview), width, height, true);
        } catch (Exception e) {
            System.out.println(imagePreview);
            e.printStackTrace();
            return null;
        }
    }

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

    public float getVoiceMessageStopProgress() {
        return voiceMessageStopPoint;
    }

    public void setVoiceMessageStopProgress(float voiceMessageStopPoint) {
        this.voiceMessageStopPoint = voiceMessageStopPoint;
    }

    public int getHeight() {
        if (height <= 0) return 200;
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        if (width <= 0) return 200;
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
