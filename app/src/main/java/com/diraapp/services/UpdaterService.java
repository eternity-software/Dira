package com.diraapp.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.utils.CacheUtils;

public class UpdaterService extends Service {


    public static final int DEFAULT_RESTART_DELAY_SEC = 60 * 50;

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

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                updateProcessor.reconnectSockets();

                updateOnlineStatus();
                handler.postDelayed(runnable, DEFAULT_RESTART_DELAY_SEC * 1000);
            }
        };

        updateProcessor = UpdateProcessor.getInstance(getApplicationContext());
        updateProcessor.reconnectSockets();

        handler.postDelayed(runnable, 15000);

        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (isOnline(context)) {
                    updateProcessor.reconnectSockets();
                }
                updateOnlineStatus();

            }
        };

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(br, intentFilter);
        updateOnlineStatus();
    }

    /**
     * Store uptime timestamp to track downtime of Dira's background service
     */
    private void updateOnlineStatus() {
        try {
            new CacheUtils(getApplicationContext()).setLong(CacheUtils.UPDATER_LAST_ACTIVE_TIME, System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);

        runnable = new Runnable() {
            public void run() {
                updateProcessor.reconnectSockets();

                handler.postDelayed(runnable, DEFAULT_RESTART_DELAY_SEC * 1000);
                updateOnlineStatus();
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
