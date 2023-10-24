package com.diraapp.ui.bottomsheet.filepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.concurrent.TimeUnit;

public class SelectorFileInfo {
    private final String name;
    private final String filePath;
    private final String mimeType;

    private boolean isSelected = false;


    public SelectorFileInfo(String name, String filePath, String mimeType) {
        this.name = name;
        this.filePath = filePath;
        this.mimeType = mimeType;
        //this.duration = getFormattedVideoDuration(context);
    }

    public static String getFormattedVideoDuration(Context context, String filePath) {

        MediaPlayer mp = MediaPlayer.create(context, Uri.parse(filePath));
        int duration = mp.getDuration();
        mp.release();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);


        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        return getFormattedNumber(minutes) + ":" + getFormattedNumber(seconds);
    }

    private static String getFormattedNumber(long num) {
        String minutesString;
        if (num < 10) {
            minutesString = "0" + num;
        } else {
            minutesString = String.valueOf(num);
        }
        return minutesString;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isImage() {
        return mimeType.startsWith("image");
    }

    public boolean isVideo() {
        return mimeType.startsWith("video");
    }

    public Bitmap getVideoThumbnail() {
        return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }
}
