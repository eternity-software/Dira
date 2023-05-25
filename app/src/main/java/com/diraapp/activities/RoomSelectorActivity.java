package com.diraapp.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.adapters.RoomSelectorAdapter;
import com.diraapp.api.requests.GetUpdatesRequest;
import com.diraapp.api.updates.ServerSyncUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.components.DiraPopup;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.notifications.Notifier;
import com.diraapp.services.UpdaterService;
import com.diraapp.storage.AppStorage;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.ProcessorListener;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RoomSelectorActivity extends AppCompatActivity implements ProcessorListener, UpdateListener {

    public static final String PENDING_ROOM_SECRET = "pendingRoomSecret";
    public static final String PENDING_ROOM_NAME = "pendingRoomName";

    private RoomSelectorAdapter roomSelectorAdapter;
    private boolean isRoomsUpdating = false;

    private CacheUtils cacheUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Thread.sleep(800);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra(PENDING_ROOM_SECRET)) {
            if (getIntent().getExtras().getString(PENDING_ROOM_SECRET) != null) {
                Intent notificationIntent = new Intent(this, RoomSelectorActivity.class);
                RoomActivity.pendingRoomSecret = getIntent().getExtras().getString(PENDING_ROOM_SECRET);
                RoomActivity.pendingRoomName = getIntent().getExtras().getString(PENDING_ROOM_NAME);
                startActivity(notificationIntent);
            }
        }

        cacheUtils = new CacheUtils(getApplicationContext());

        if (!cacheUtils.hasKey(CacheUtils.AUTO_LOAD_SIZE)) {
            cacheUtils.setLong(CacheUtils.AUTO_LOAD_SIZE, AppStorage.MAX_DEFAULT_ATTACHMENT_AUTOLOAD_SIZE);
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


        if (!cacheUtils.hasKey(CacheUtils.ID)) {
            cacheUtils.setString(CacheUtils.ID, KeyGenerator.generateId());
            cacheUtils.setString(CacheUtils.NICKNAME, getString(R.string.dira_user) + " " + new Random().nextInt(1000));
        }


        UpdateProcessor.getInstance(getApplicationContext()).addProcessorListener(this);
        UpdateProcessor.getInstance(getApplicationContext()).addUpdateListener(this);

        updateRooms();
        askForPermissions();


    }


    private void askForPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        }


        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION);
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

    private void updateRooms() {
        if (isRoomsUpdating) return;
        isRoomsUpdating = true;
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Thread loadDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                List<Room> rooms = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getAllRoomsByUpdatedTime();
                roomSelectorAdapter = new RoomSelectorAdapter(RoomSelectorActivity.this);
                for (Room room : new ArrayList<>(rooms)) {
                    Message message = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao().getMessageById(room.getLastMessageId());
                    room.setMessage(message);
                    try {

                        UpdateProcessor.getInstance().sendRequest(new GetUpdatesRequest(room.getSecretName(), room.getLastUpdateId()));
                    } catch (Exception ignored) {
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
        String picPath = cacheUtils.getString(CacheUtils.PICTURE);
        if (picPath != null) imageView.setImageBitmap(AppStorage.getImage(picPath));

        Notifier.cancelAllNotifications(getApplicationContext());
    }

    @Override
    public void onSocketsCountChange(float percentOpened) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = findViewById(R.id.status_light);
                if (percentOpened != 1) {
                    if (percentOpened == 0) {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                        UpdateProcessor.getInstance(getApplicationContext()).reconnectSockets();
                    } else {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);

                    }
                    imageView.setVisibility(View.VISIBLE);

                } else {
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
        if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            updateRooms();
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            updateRooms();
        } else if (update.getUpdateType() == UpdateType.SERVER_SYNC) {
            if(!((ServerSyncUpdate) update).getSupportedApis().contains(UpdateProcessor.API_VERSION))
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DiraPopup diraPopup = new DiraPopup(RoomSelectorActivity.this);
                        diraPopup.setCancellable(false);
                        diraPopup.show(getString(R.string.unsupported_api_title),
                                getString(R.string.unsupported_api_text),
                                null,
                                null, new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });
                    }
                });
            }
        }
    }
}