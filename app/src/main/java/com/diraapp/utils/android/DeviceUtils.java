package com.diraapp.utils.android;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.TypedValue;

import java.io.File;
import java.util.Objects;

public class DeviceUtils {


    // Конвертируем dp в пикселы
    public static int dpToPx(float dp, Context context) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return (int) px;
    }

    public static int pxToDp(float toDP, Context context) {
        if (toDP == 0) {
            return 0;
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            return (int) Math.ceil((density * toDP));
        }
    }


    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }


    // Получаем локальное время из timestamp
    public static String getTimeFromTimestamp(long timestamp) {
        return DateFormat.format("HH:mm", timestamp).toString();
    }


    public static String getDateFromTimestamp(long timestamp, boolean hasYear) {
        String format = "dd MMMM ";
        if (hasYear) format += "yyy";
        return DateFormat.format(format, timestamp).toString();
    }

    public static String getShortDateFromTimestamp(long timestamp) {
        String format = "dd.mm.yyyy";
        return DateFormat.format(format, timestamp).toString();
    }

    public static String getDurationTimeMS(long millis) {
        long seconds = millis / 1000;

        long s = seconds % 60;
        String st = String.valueOf(s);
        if (st.length() == 1) st = "0" + st;

        long m = (seconds / 60) % 60;
        String mt = String.valueOf(m);
        if (mt.length() == 1) mt = "0" + mt;

        return mt + ":" + st;
    }

    public static long readDuration(File file, Context context) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(context, Uri.fromFile(file));

            long result = Long.parseLong(Objects.requireNonNull(
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

            metaRetriever.release();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 60_000;
        }
    }

    public static boolean getBooleanFromInt(int i) {
        return i == 1;

    }

}
