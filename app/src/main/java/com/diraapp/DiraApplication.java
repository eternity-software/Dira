package com.diraapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.notifications.Notifier;
import com.diraapp.ui.activities.CrashActivity;

import java.io.PrintWriter;
import java.io.StringWriter;


public class DiraApplication extends Application implements LifecycleObserver {

    private static boolean isBackgrounded = true;

    public static boolean isBackgrounded() {
        return isBackgrounded;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable e) {


                        Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        String stackTrace = sw.toString(); // stack trace as a string
                        intent.putExtra("ex", stackTrace);
                        startActivity(intent);
                        Log.i("DiraApp", "3");
                        System.exit(1);
                        CrashActivity.PENDING_ERROR = e;

                    }
                });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {
        isBackgrounded = true;
        Log.d("Dira", "App in background");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForegrounded() {
        isBackgrounded = false;
        try {
            Notifier.cancelAllNotifications(getApplicationContext());
            UpdateProcessor.getInstance(getApplicationContext()).reconnectSockets();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Dira", "App in foreground");
    }
}
