package com.diraapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.TypedValue;

public class Numbers {


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

    public static boolean getBooleanFromInt(int i) {
        return i == 1;

    }

}
