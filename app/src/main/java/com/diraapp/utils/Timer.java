package com.diraapp.utils;

import android.util.Log;

public class Timer {


    private static final long WARN_TIME = 1;
    private final String name;
    private long startTime = 0;

    public Timer(String name) {
        this.name = name;
        this.startTime = System.currentTimeMillis();
    }

    public void reportTime() {
        if (System.currentTimeMillis() - startTime >= WARN_TIME) {
            Log.w("TIMER", name + " took " + (System.currentTimeMillis() - startTime));
        }
    }
}
