package com.diraapp.ui.activities.legacy;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.GetUpdatesRequest;
import com.diraapp.api.updates.MessageReadUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.ServerSyncUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.userstatus.UserStatusHandler;
import com.diraapp.api.userstatus.UserStatusListener;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.exceptions.LanguageParsingException;
import com.diraapp.notifications.Notifier;
import com.diraapp.res.Theme;
import com.diraapp.services.UpdaterService;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.JoinRoomActivity;
import com.diraapp.ui.activities.PersonalityActivity;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.ui.adapters.selector.RoomSelectorAdapter;
import com.diraapp.ui.adapters.selector.SelectorAdapterContract;
import com.diraapp.ui.adapters.selector.SelectorViewHolder;
import com.diraapp.ui.adapters.selector.SelectorViewHolderNotifier;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.ui.components.GlobalPlayerComponent;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RoomSelectorActivity extends AppCompatActivity
        implements ProcessorListener, UpdateListener, UserStatusListener,
        SelectorAdapterContract {

    public static final String PENDING_ROOM_SECRET = "pendingRoomSecret";
    public static final String PENDING_ROOM_NAME = "pendingRoomName";

    public static final String CAN_BE_BACK_PRESSED = "canBackPressed";
    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };
    private boolean canBackPress = true;
    private RoomSelectorAdapter roomSelectorAdapter;
    private boolean isRoomsUpdating = false;
    private List<Room> roomList = new ArrayList<>();
    private CacheUtils cacheUtils;

    private RecyclerView recyclerView;

    private String lastOpenedRoomId = null;

    private RoomDao roomDao;

    private MessageDao messageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Theme.loadCurrentTheme(this);
        } catch (LanguageParsingException e) {

        }

        setContentView(R.layout.activity_main);


        if (getIntent().hasExtra(PENDING_ROOM_SECRET)) {
            if (getIntent().getExtras().getString(PENDING_ROOM_SECRET) != null) {
                Intent notificationIntent = new Intent(this, RoomActivity.class);

                lastOpenedRoomId = getIntent().getExtras().getString(PENDING_ROOM_SECRET);


                RoomActivity.putRoomExtrasInIntent(notificationIntent,
                        lastOpenedRoomId, getIntent().getExtras().getString(PENDING_ROOM_NAME));
                startActivity(notificationIntent);
            }
        }


        cacheUtils = new CacheUtils(getApplicationContext());
        roomDao = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao();
        messageDao = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao();

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

        updateRooms(true);


        if (!hasAllCriticalPermissions()) {
            askForPermissions();
        }

        if (getIntent().hasExtra(CAN_BE_BACK_PRESSED)) {
            canBackPress = getIntent().getExtras().getBoolean(CAN_BE_BACK_PRESSED);
        }

        UserStatusHandler.getInstance().addListener(this);
        UserStatusHandler.getInstance().startThread();
    }

    private void askForPermissions() {
        DiraPopup diraPopup = new DiraPopup(RoomSelectorActivity.this);
        diraPopup.setCancellable(false);
        diraPopup.show(getString(R.string.permissions_request_title),
                getString(R.string.permissions_request_text) + " " + getBadPermissionString(),
                null,
                null, new Runnable() {
                    @Override
                    public void run() {
                        List<String> permissions = getPermissions();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                            Intent intent = new Intent();
                            String packageName = getPackageName();
                            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                                for (Intent intent2 : POWERMANAGER_INTENTS) {
                                    if (getPackageManager().resolveActivity(
                                            intent,
                                            PackageManager.MATCH_DEFAULT_ONLY
                                    ) != null) {
                                        startActivityForResult(intent2, 1234);
                                    }

                                }
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);
                            }

                        }
                        ActivityCompat.requestPermissions(RoomSelectorActivity.this, permissions.toArray(new String[permissions.size()]), 3);

                    }
                });

    }

    private boolean hasAllCriticalPermissions() {

        for (String permission : getPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY) &&
                        !permission.equals(Manifest.permission.ACCESS_MEDIA_LOCATION)) {
                    Logger.logDebug(this.getClass().getSimpleName(),
                            "Permission not granted: " + permission);
                    return false;
                }
            }
        }

        return true;

    }


    private String getBadPermissionString() {

        for (String permission : getPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (!permission.equals(Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                    Logger.logDebug(this.getClass().getSimpleName(),
                            "Permission not granted: " + permission);
                    return permission;
                }
            }
        }

        return null;

    }


    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
        }


        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.CAMERA);


        return permissions;

    }

    private void updateRooms() {
        updateRooms(false);
    }

    private void notifyViewHolder(int pos, SelectorViewHolderNotifier notifier) {
        SelectorViewHolder holder = (SelectorViewHolder) recyclerView.
                findViewHolderForAdapterPosition(pos);

        boolean notFound = holder == null;

        if (notFound) {
            roomSelectorAdapter.notifyItemChanged(pos);
            return;
        }

        notifier.notifyViewHolder(holder);
    }

    private void updateRooms(boolean updateRooms) {
        if (isRoomsUpdating) return;
        isRoomsUpdating = true;
        recyclerView = findViewById(R.id.recycler_view);
        Thread loadDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    roomList = roomDao.getAllRoomsByUpdatedTime();
                    roomSelectorAdapter = new RoomSelectorAdapter(
                            RoomSelectorActivity.this, RoomSelectorActivity.this);

                    for (Room room : new ArrayList<>(roomList)) {
                        Logger.logDebug(room.getName(), "" + room.getLastUpdatedTime());
                        Message message = messageDao.getMessageAndAttachmentsById(room.getLastMessageId());
                        room.setMessage(message);
                    }

                    if (updateRooms) {
                        HashMap<String, GetUpdatesRequest> requests = UpdateProcessor.getInstance().
                                createGetUpdatesRequests(roomList);

                        for (String serverAddress : requests.keySet()) {
                            try {
                                UpdateProcessor.getInstance().sendRequest(requests.get(serverAddress), serverAddress);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            roomSelectorAdapter.setRoomList(roomList);
                            roomSelectorAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(roomSelectorAdapter);
                            isRoomsUpdating = false;
                        }
                    });
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        });
        loadDataThread.start();
    }

    private void updateRoom(String roomSecret, boolean withPositionUpdating) {
        updateRoom(roomSecret, withPositionUpdating, null);
    }

    private void updateRoom(String roomSecret, boolean withPositionUpdating,
                            OnRoomLoadedListener onLoadedCallback) {
        DiraActivity.runGlobalBackground(() -> {

            Logger.logDebug(RoomSelectorActivity.class.getName(), "update room - " + roomSecret);
            Room room = roomDao.getRoomBySecretName(roomSecret);

            if (room == null) {
                Logger.logDebug(RoomSelectorActivity.class.getName(), "room = null");
                return;
            }

            if (onLoadedCallback != null) {
                if (!onLoadedCallback.keepUpdate(room)) return;
            }

            Logger.logDebug(RoomSelectorActivity.class.getName(), "name - " + room.getName());

            Message message = messageDao.getMessageAndAttachmentsById(room.getLastMessageId());
            room.setMessage(message);

            runOnUiThread(() -> {
                final int NOT_FOUND = -1;
                final int FIRST_POSITION = 0;
                int pos = NOT_FOUND;
                for (int i = 0; i < roomList.size(); i++) {
                    Room r = roomList.get(i);
                    if (r.getSecretName().equals(roomSecret)) {
                        pos = i;
                        break;
                    }
                }

                if (pos == NOT_FOUND) {
                    Logger.logDebug(RoomSelectorActivity.class.toString(),
                            "pos = -1 -> is it a new room?");

                    roomList.add(0, room);
                    roomSelectorAdapter.notifyItemInserted(0);

                    recyclerView.scrollToPosition(0);
                    return;
                }

                // If item needs to be moved
                if (withPositionUpdating && pos != FIRST_POSITION) {

                    roomList.remove(pos);
                    roomSelectorAdapter.notifyItemRemoved(pos);

                    roomList.add(0, room);
                    roomSelectorAdapter.notifyItemInserted(0);

                    int higthestVisiblePosition = -1;
                    LinearLayoutManager layoutManager = (LinearLayoutManager)
                            recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        higthestVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    }

                    boolean isVisible = higthestVisiblePosition == 0;
                    if (isVisible) {
                        recyclerView.scrollBy(0, -DeviceUtils.dpToPx(76, this));
                    }

                    return;

                }

                roomList.set(pos, room);

                notifyViewHolder(pos, new SelectorViewHolderNotifier() {
                    @Override
                    public void notifyViewHolder(SelectorViewHolder holder) {
                        holder.onBind(room);
                    }
                });


            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        //updateRooms(true);
        ImageView imageView = findViewById(R.id.profile_picture);
        String picPath = cacheUtils.getString(CacheUtils.PICTURE);
        if (picPath != null) imageView.setImageBitmap(AppStorage.getBitmapFromPath(picPath));

        Notifier.cancelAllNotifications(getApplicationContext());

        if (lastOpenedRoomId == null) return;
        for (int i = 0; i < roomList.size(); i++) {
            Room room = roomList.get(i);
            if (lastOpenedRoomId.equals(room.getSecretName())) {
                updateRoom(lastOpenedRoomId, false, (Room currentRoom) -> {
                    lastOpenedRoomId = null;
                    return true;
                });
                return;
            }
        }
    }

    @Override
    public void onSocketsCountChange(float percentOpened) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = findViewById(R.id.status_light);
                if (percentOpened != 1) {
                    if (percentOpened == 0) {
                        imageView.setColorFilter(Theme.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                        UpdateProcessor.getInstance(getApplicationContext()).reconnectSockets();
                    } else {
                        imageView.setColorFilter(Theme.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);

                    }
                    imageView.setVisibility(View.VISIBLE);

                } else {
                    findViewById(R.id.status_light).setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (canBackPress) {
            super.onBackPressed();
        }
    }

    public void setCanBackPress(boolean canBackPress) {
        this.canBackPress = canBackPress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateProcessor.getInstance().removeUpdateListener(this);

        UserStatusHandler.getInstance().removeListener(this);

        GlobalMediaPlayer.getInstance().release();

        GlobalPlayerComponent component = findViewById(R.id.global_player);
        component.release();
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            updateRoom(((NewMessageUpdate) update).getMessage().getRoomSecret(), true);
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            updateRoom(update.getRoomSecret(), false);
        } else if (update.getUpdateType() == UpdateType.SERVER_SYNC) {
            if (!((ServerSyncUpdate) update).getSupportedApis().contains(UpdateProcessor.API_VERSION)) {
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
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            MessageReadUpdate readUpdate = ((MessageReadUpdate) update);

            onReadUpdate(readUpdate);
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            Room room = null;
            for (Room r : roomList) {
                if (r.getSecretName().equals(update.getRoomSecret())) {
                    room = r;
                    break;
                }
            }

            if (room == null) return;

            if (room.getRoomType() == RoomType.PRIVATE) {
                updateRoom(update.getRoomSecret(), false);
            }
        }
    }

    private void onReadUpdate(MessageReadUpdate update) {
        Room currentRoom = null;
        int position = 0;
        for (int i = 0; i < roomList.size(); i++) {
            Room room = roomList.get(i);
            if (room.getSecretName().equals(update.getRoomSecret())) {
                currentRoom = room;
                position = i;
                break;
            }
        }

        if (currentRoom == null) {
            updateRoom(update.getRoomSecret(), false,
                    (Room room) -> update.getMessageId().equals(room.getLastMessageId()));
            return;
        }

        Message message = currentRoom.getMessage();
        MessageReading reading = new MessageReading(update.getUserId(), update.getReadTime());
        message.addReadingIfAvailable(reading);

        boolean isLastMessage = update.getMessageId().equals(currentRoom.getLastMessageId());

        if (!isLastMessage) return;

        int finalPosition = position;
        runOnUiThread(() -> {
            notifyViewHolder(finalPosition, new SelectorViewHolderNotifier() {
                @Override
                public void notifyViewHolder(SelectorViewHolder holder) {
                    holder.bindReading(message);
                }
            });

        });
    }

    @Override
    public void updateRoomStatus(String secretName, ArrayList<UserStatus> userUserStatusList) {
        //
    }

    @Override
    public void onRoomOpen(String roomSecret) {
        lastOpenedRoomId = roomSecret;
    }

    private interface OnRoomLoadedListener {
        boolean keepUpdate(Room room);
    }
}