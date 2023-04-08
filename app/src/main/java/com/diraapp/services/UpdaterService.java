package com.diraapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.diraapp.updates.UpdateProcessor;

public class UpdaterService extends Service {


    public static Runnable runnable = null;
    public static UpdateProcessor updateProcessor;
    public Context context = this;
    public Handler handler = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();


        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                updateProcessor.reconnectSockets();

                handler.postDelayed(runnable, 10000 * 40);
            }
        };

        updateProcessor = UpdateProcessor.getInstance(getApplicationContext());
        updateProcessor.reconnectSockets();

        handler.postDelayed(runnable, 15000);
    }


    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        //  Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
}
