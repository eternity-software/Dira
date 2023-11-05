package com.diraapp.db.entities;

import android.content.res.Resources;
import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.diraapp.storage.AppStorage;
import com.diraapp.utils.Logger;

@Entity
public class Attachment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String fileUrl;
    private long fileCreatedTime;
    private String fileName;
    private long size;
    private String displayFileName = "";
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
            Bitmap bitmap = AppStorage.getBitmapFromBase64(imagePreview);

            float scale = calculateWidthScale(50);
            return Bitmap.createScaledBitmap(bitmap, (int) (width * scale),
                    (int) (height * scale), true);
        } catch (Exception e) {
            Logger.logDebug(getClass().getSimpleName(), "Not found preview for " + attachmentType.name());
            e.printStackTrace();
            return null;
        }
    }

    public float calculateDisplayDependScaleFactor() {
        int width = getWidth();
        if (width > Resources.getSystem().getDisplayMetrics().widthPixels) {

            float scale = (float) Resources.getSystem().getDisplayMetrics().widthPixels * 0.75f / width;

            return scale;

        }
        return 1;
    }


    public float calculateWidthScale(int maxWidth) {
        int width = getWidth();


        float scale = (float) maxWidth / width;

        if (scale < 1)
            return scale;


        return 1;
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

    public String getDisplayFileName() {
        return displayFileName;
    }

    public void setDisplayFileName(String displayFileName) {
        this.displayFileName = displayFileName;
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

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileCreatedTime=" + fileCreatedTime +
                ", fileName='" + fileName + '\'' +
                ", size=" + size +
                ", attachmentType=" + attachmentType +
                ", height=" + height +
                ", width=" + width +
                ", imagePreview='" + imagePreview + '\'' +
                ", voiceMessageStopPoint=" + voiceMessageStopPoint +
                '}';
    }
}
