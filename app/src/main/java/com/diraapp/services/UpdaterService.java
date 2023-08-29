package com.diraapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.diraapp.DiraApplication;
import com.diraapp.api.processors.UpdateProcessor;

public class UpdaterService extends Service {


    private static final int DEFAULT_RESTART_DELAY_SEC = 120;

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


                // Delay for receiving updates and sleep for battery economy
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (DiraApplication.isBackgrounded()) {
                            try {
                                Thread.sleep(10000);
                                updateProcessor.disconnectSockets();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
                thread.start();


                handler.postDelayed(runnable, DEFAULT_RESTART_DELAY_SEC * 1000);
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

        runnable = new Runnable() {
            public void run() {
                updateProcessor.reconnectSockets();

                handler.postDelayed(runnable, DEFAULT_RESTART_DELAY_SEC * 1000);
            }
        };
        handler.postDelayed(runnable, 15000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
}
