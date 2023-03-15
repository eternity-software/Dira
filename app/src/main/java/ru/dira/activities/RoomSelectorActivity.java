package ru.dira.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.dira.R;
import ru.dira.adapters.RoomSelectorAdapter;
import ru.dira.api.SocketClient;
import ru.dira.api.requests.GetUpdatesRequest;
import ru.dira.api.updates.Update;
import ru.dira.api.updates.UpdateType;
import ru.dira.attachments.ImageStorage;
import ru.dira.db.DiraMessageDatabase;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;
import ru.dira.notifications.Notifier;
import ru.dira.services.UpdateListener;
import ru.dira.services.UpdateProcessor;
import ru.dira.services.UpdateProcessorListener;
import ru.dira.services.UpdaterService;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.KeyGenerator;

public class RoomSelectorActivity extends AppCompatActivity implements UpdateProcessorListener, UpdateListener {

    public static final String PENDING_ROOM_SECRET = "pendingRoomSecret";
    public static final String PENDING_ROOM_NAME = "pendingRoomName";

    private RoomSelectorAdapter roomSelectorAdapter;
    private boolean isRoomsUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
           // Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(getIntent().hasExtra(PENDING_ROOM_SECRET)) {
            if (getIntent().getExtras().getString(PENDING_ROOM_SECRET) != null) {
                Intent notificationIntent = new Intent(this, RoomSelectorActivity.class);
                RoomActivity.pendingRoomSecret = getIntent().getExtras().getString(PENDING_ROOM_SECRET);
                RoomActivity.pendingRoomName = getIntent().getExtras().getString(PENDING_ROOM_NAME);
                startActivity(notificationIntent);
            }
        }

        startService(new Intent(this, UpdaterService.class));

        findViewById(R.id.button_new_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomSelectorActivity.this, JoinRoomActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.personality_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomSelectorActivity.this, PersonalityActivity.class);
                startActivity(intent);
            }
        });

        if(!CacheUtils.getInstance().hasKey(CacheUtils.ID, getApplicationContext()))
        {
            CacheUtils.getInstance().setString(CacheUtils.ID, KeyGenerator.generateId(), getApplicationContext());
            CacheUtils.getInstance().setString(CacheUtils.NICKNAME, getString(R.string.dira_user) + " " + new Random().nextInt(1000), getApplicationContext());
        }


        UpdateProcessor.getInstance(getApplicationContext()).addProcessorListener(this);
        UpdateProcessor.getInstance(getApplicationContext()).addUpdateListener(this);

        updateRooms();
        askForPermissions();


    }



    private void askForPermissions()
    {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
          permissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        }
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 3);


    }

    private void updateRooms()
    {
        if(isRoomsUpdating) return;
        isRoomsUpdating = true;
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Thread loadDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Room> rooms = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getAllRoomsByUpdatedTime();
                roomSelectorAdapter = new RoomSelectorAdapter(RoomSelectorActivity.this);
                for(Room room : new ArrayList<>(rooms))
                {
                   Message message = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao().getMessageById(room.getLastMessageId());
                   room.setMessage(message);
                   try {

                       UpdateProcessor.getInstance().sendRequest(new GetUpdatesRequest(room.getSecretName(), room.getLastUpdateId()));
                   }
                   catch (Exception ignored)
                   {
                       ignored.printStackTrace();

                   }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        roomSelectorAdapter.setRoomList(rooms);
                        roomSelectorAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(roomSelectorAdapter);
                        isRoomsUpdating = false;
                    }
                });
            }
        });
        loadDataThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();


        updateRooms();
        ImageView imageView = findViewById(R.id.profile_picture);
        String picPath = CacheUtils.getInstance().getString(CacheUtils.PICTURE, getApplicationContext());
        if(picPath != null) imageView.setImageBitmap(ImageStorage.getImage(picPath));

        Notifier.cancelAllNotifications(getApplicationContext());
    }

    @Override
    public void onSocketsCountChange(float percentOpened) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = findViewById(R.id.status_light);
                if(percentOpened != 1)
                {
                    if(percentOpened == 0)
                    {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                    else
                    {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow),android.graphics.PorterDuff.Mode.SRC_IN);

                    }
                    imageView.setVisibility(View.VISIBLE);
                }
                else
                {
                    findViewById(R.id.status_light).setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateProcessor.getInstance().removeUpdateListener(this);
    }

    @Override
    public void onUpdate(Update update) {
        if(update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE)
        {
            updateRooms();
        }
        else if(update.getUpdateType() == UpdateType.ROOM_UPDATE)
        {
            updateRooms();
        }
    }
}