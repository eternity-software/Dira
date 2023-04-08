package com.diraapp.utils;

import android.content.Context;
import android.text.format.DateFormat;

public class TimeConverter {

    public static String getTimeFromTimestamp(long timestamp, Context context) {
        return DateFormat.format("HH:mm", timestamp).toString();
    }

    public static String getDateFromTimestamp(String timestamp, Context context) {
        return DateFormat.format("EEEE, d MMMM", Long.parseLong(timestamp)).toString();
    }
}
