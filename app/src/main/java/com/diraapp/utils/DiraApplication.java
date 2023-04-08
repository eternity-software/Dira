package com.diraapp.utils;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;


public class DiraApplication extends Application implements LifecycleObserver {

    private static boolean isBackgrounded = true;

    public static boolean isBackgrounded() {
        return isBackgrounded;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {
        isBackgrounded = true;
        Log.d("Dira", "App in background");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForegrounded() {
        isBackgrounded = false;
        Log.d("Dira", "App in foreground");
    }
}
