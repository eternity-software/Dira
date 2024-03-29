package com.diraapp.utils;

import android.content.Context;
import android.text.format.DateFormat;

import com.diraapp.R;

public class TimeConverter {

    public static String getTimeFromTimestamp(long timestamp) {
        return DateFormat.format("HH:mm", timestamp).toString();
    }

    public static String getFormattedTimeAfterTimestamp(long timestamp, Context context) {
        long time = System.currentTimeMillis() - timestamp;
        long timeSec = time / 1000L;

        if (timeSec < 60) {
            return context.getResources().getString(R.string.less_than_a_minute);
        }

        int count = 1;
        String suffix = "unknown";

        if (timeSec < 60 * 60) {
            count = (int) (timeSec / 60);
            if (count > 1) {
                suffix = context.getString(R.string.time_unit_minutes);
            } else {
                suffix = context.getString(R.string.time_unit_minute);
            }
        } else if (timeSec < 24 * 60 * 60) {
            count = (int) (timeSec / 60 / 60);
            if (count > 1) {
                suffix = context.getString(R.string.time_unit_hours);
            } else {
                suffix = context.getString(R.string.time_unit_hour);
            }
        } else if (timeSec < 24 * 60 * 60 * 60) {
            count = (int) (timeSec / 60 / 60 / 24);
            if (count > 1) {
                suffix = context.getString(R.string.time_unit_days);
            } else {
                suffix = context.getString(R.string.time_unit_day);
            }
        }


        return count + " " + suffix;
    }


    public static String getDateFromTimestamp(String timestamp) {
        return DateFormat.format("EEEE, d MMMM", Long.parseLong(timestamp)).toString();
    }
}
