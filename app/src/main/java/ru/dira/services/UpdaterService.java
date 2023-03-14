package ru.dira.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.google.gson.Gson;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.dira.api.SocketClient;
import ru.dira.api.requests.Request;
import ru.dira.api.updates.Update;
import ru.dira.api.updates.UpdateDeserializer;

public class UpdaterService extends Service {


    public Context context = this;
    public Handler handler = null;

    public static Runnable runnable = null;

    public static UpdateProcessor updateProcessor;

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

                handler.postDelayed(runnable, 10000);
            }
        };

        updateProcessor = UpdateProcessor.getInstance(getApplicationContext());






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
