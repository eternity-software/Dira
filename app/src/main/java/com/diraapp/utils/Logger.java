package com.diraapp.utils;

import android.util.Log;

import com.diraapp.BuildConfig;

public class Logger {

    public static void logDebug(String tag, String log) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, log);
        }
    }
}
