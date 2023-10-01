package com.diraapp.utils;

import android.text.format.DateFormat;

public class TimeConverter {

    public static String getTimeFromTimestamp(long timestamp) {
        return DateFormat.format("HH:mm", timestamp).toString();
    }

    public static String getDateFromTimestamp(String timestamp) {
        return DateFormat.format("EEEE, d MMMM", Long.parseLong(timestamp)).toString();
    }
}
