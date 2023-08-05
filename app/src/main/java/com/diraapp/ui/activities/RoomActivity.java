package com.diraapp.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.api.requests.SendUserStatusRequest;
import com.diraapp.api.updates.MessageReadUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.userstatus.Status;
import com.diraapp.api.updates.UserStatusUpdate;
import com.diraapp.api.views.UserStatus;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.notifications.Notifier;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.storage.images.FilesUploader;
import com.diraapp.ui.activities.resizer.FluidContentResizer;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messages.RoomMessagesAdapter;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.userstatus.UserStatusHandler;
import com.diraapp.userstatus.UserStatusListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.EncryptionUtil;
import com.diraapp.utils.SliderActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class RoomActivity extends AppCompatActivity
        implements UpdateListener, ProcessorListener, UserStatusListener {
    private String roomSecret;
    private Room room;
    private RoomMessagesAdapter roomMessagesAdapter;
    private List<Message> messageList = new ArrayList<>();
    private FilePickerBottomSheet filePickerBottomSheet;

    private String selfId;

    private Status userStatus;
    private boolean isUpdating = false;

    private boolean keepThread = true;

    private AppTheme theme;

    public static void putRoomExtrasInIntent(Intent intent, String roomSecret, String roomName) {
        intent.putExtra(RoomSelectorActivity.PENDING_ROOM_SECRET, roomSecret);
        intent.putExtra(RoomSelectorActivity.PENDING_ROOM_NAME, roomName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        roomSecret = getIntent().getExtras().getString(RoomSelectorActivity.PENDING_ROOM_SECRET);
        String roomName = getIntent().getExtras().getString(RoomSelectorActivity.PENDING_ROOM_NAME);
        selfId = new CacheUtils(this).getString(CacheUtils.ID);

        TextView nameView = findViewById(R.id.room_name);
        nameView.setText(roomName);

        UpdateProcessor.getInstance().addProcessorListener(this);

        theme = AppTheme.getInstance();
        applyColorTheme();

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.room_info_pan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this, RoomInfoActivity.class);
                intent.putExtra(RoomInfoActivity.ROOM_SECRET_EXTRA, roomSecret);


                //  Pair<View, String> p1 = Pair.create(findViewById(R.id.room_picture), "icon");
                //Pair<View, String> p2 = Pair.create(findViewById(R.id.room_name), "name");


                // ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(RoomActivity.this, p1);

                startActivity(intent);
            }
        });


        setupMessageTextInputListener();
        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.message_text_input);
                if (sendTextMessage(editText.getText().toString())) {
                    editText.setText("");
                }
            }
        });


        UpdateProcessor.getInstance().addUpdateListener(this);


        findViewById(R.id.attach_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePickerBottomSheet = new FilePickerBottomSheet();
                filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
                filePickerBottomSheet.setRunnable(new MediaGridItemListener() {
                    @Override
                    public void onItemClick(int pos, final View view) {
                        ImageSendActivity.open(RoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                                (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_MESSAGE);


                    }

                    @Override
                    public void onLastItemLoaded(int pos, View view) {

                    }
                });
            }
        });

        Notifier.cancelAllNotifications(getApplicationContext());

        FluidContentResizer fluidContentResizer = new FluidContentResizer();
        fluidContentResizer.listen(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);


        Thread loadMessagesHistory = new Thread(new Runnable() {
            @Override
            public void run() {
                Room room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
                roomMessagesAdapter = new RoomMessagesAdapter(RoomActivity.this, roomSecret, room.getServerAddress(), room, new RoomMessagesAdapter.MessageAdapterListener() {
                    @Override
                    public void onFirstItemScrolled(Message message, int index) {
                        Thread loadPreviousMessages = new Thread(() -> {

                            try {
                                MessageDao messageDao = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao();
                                System.out.println("Time from " + message.getTime());
                                List<Message> oldMessages = messageDao.getLastMessagesInRoom(roomSecret, message.getTime());
                                int oldSize = messageList.size();
                                messageList.addAll(oldMessages);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        recyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                roomMessagesAdapter.notifyDataSetChanged();
                                            }
                                        });

                                    }
                                });
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        });
                        loadPreviousMessages.start();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                });

                messageList = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao().getLastMessagesInRoom(roomSecret);
                roomMessagesAdapter.setMessages(messageList);
                loadMembers();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        recyclerView.setAdapter(roomMessagesAdapter);
                        roomMessagesAdapter.notifyDataSetChanged();

                        updateUserStatus(roomSecret, UserStatusHandler.getInstance().getUserStatuses(roomSecret));

                    }
                });
            }
        });
        loadMessagesHistory.start();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode != RESULT_OK && resultCode != ImageSendActivity.CODE) {
                return;
            }
            if (requestCode == 2) {
                final Bundle extras = data.getExtras();
                if (extras != null) {


                }
            }
            if (resultCode == ImageSendActivity.CODE) {
                if (filePickerBottomSheet != null) {

                    /**
                     * Throws an java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                     * on some devices (tested on Android 5.1)
                     */
                    try {
                        filePickerBottomSheet.dismiss();
                    } catch (Exception ignored) {
                    }
                }
                final String messageText = data.getStringExtra("text");
                String imageUri = data.getStringExtra("uri");


                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("uploading...");


                            if (FileClassifier.isVideoFile(imageUri)) {
                                List<Uri> urisToCompress = new ArrayList<>();
                                urisToCompress.add(Uri.fromFile(new File(imageUri)));
                                System.out.println("compression started");
                                VideoCompressor.start(getApplicationContext(), urisToCompress,
                                        false,
                                        null,
                                        new AppSpecificStorageConfiguration(
                                                new File(imageUri).getName() + "temp_compressed", null), // => required name
                                        new Configuration(VideoQuality.VERY_LOW,
                                                false,
                                                2,
                                                false,
                                                false,
                                                null,
                                                null), new CompressionListener() {
                                            @Override
                                            public void onStart(int i) {

                                            }

                                            @Override
                                            public void onSuccess(int i, long l, @Nullable String s) {
                                                if (s != null) {
                                                    try {
                                                        FilesUploader.uploadFile(s, createAttachmentCallback(s, messageText, AttachmentType.VIDEO), getApplicationContext(), true, room.getServerAddress(), room.getEncryptionKey());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(int i, @NonNull String s) {
                                                System.out.println("compression fail " + s);
                                            }

                                            @Override
                                            public void onProgress(int i, float v) {
                                                System.out.println("compression progress " + i + "  " + v);
                                            }

                                            @Override
                                            public void onCancelled(int i) {
                                                System.out.println("compression canceled");
                                            }
                                        });
                            } else {
                                try {
                                    FilesUploader.uploadFile(imageUri, createAttachmentCallback(imageUri, messageText, AttachmentType.IMAGE), getApplicationContext(), false, room.getServerAddress(), room.getEncryptionKey());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                    thread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Callback createAttachmentCallback(String fileUri, String messageText, AttachmentType attachmentType) throws IOException {
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                System.out.println(":(");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("uploading");

                try {
                    String fileTempName = new JSONObject(response.body().string()).getString("message");
                    Attachment attachment = new Attachment();


                    attachment.setAttachmentType(attachmentType);


                    attachment.setFileCreatedTime(System.currentTimeMillis());
                    attachment.setFileName("attachment");
                    System.out.println("uploaded url " + fileTempName);
                    attachment.setFileUrl(fileTempName);
                    attachment.setSize(new File(fileUri).length());

                    Message message = Message.generateMessage(getApplicationContext(), roomSecret);

                    message.setLastTimeEncryptionKeyUpdated(room.getTimeEncryptionKeyUpdated());

                    if (room.getEncryptionKey().equals("")) {
                        message.setText(messageText);
                    } else {
                        message.setText(EncryptionUtil.encrypt(messageText, room.getEncryptionKey()));
                    }

                    message.getAttachments().add(attachment);

                    SendMessageRequest sendMessageRequest = new SendMessageRequest(message);

                    UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void loadMembers() {
        List<Member> memberList = DiraRoomDatabase.getDatabase(getApplicationContext()).getMemberDao().getMembersByRoomSecret(roomSecret);

        HashMap<String, Member> memberHashMap = new HashMap<>();


        for (Member member : memberList) {
            System.out.println(member.getNickname());
            memberHashMap.put(member.getId(), member);
        }
        roomMessagesAdapter.setMembers(memberHashMap);
    }

    private void loadData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
                room.setUpdatedRead(true);
                DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);

                loadMembers();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (room.getImagePath() != null) {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
                        }

                        TextView roomName = findViewById(R.id.room_name);
                        applyColorTheme();

                        roomName.setText(room.getName());
                    }
                });
            }
        });
        thread.start();
    }

    public boolean sendTextMessage(String text) {

        while (text.contains("   ")) {
            text = text.replace("   ", " ");
        }

        while (text.contains("\n\n\n")) {
            text = text.replace("\n\n\n", "\n");
        }

        if (text.replace(" ", "").replace("\n", "").length() != 0) {
            Message message = Message.generateMessage(getApplicationContext(), roomSecret);
            message.setLastTimeEncryptionKeyUpdated(room.getTimeEncryptionKeyUpdated());

            if (room.getEncryptionKey().equals("")) {
                message.setText(text);
            } else {
                message.setText(EncryptionUtil.encrypt(text, room.getEncryptionKey()));
            }

            SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
            try {
                UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());
            } catch (UnablePerformRequestException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keepThread = false;
        roomMessagesAdapter.unregisterListeners();
        UpdateProcessor.getInstance().removeUpdateListener(this);
        UpdateProcessor.getInstance().removeProcessorListener(this);
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            NewMessageUpdate newMessageUpdate = (NewMessageUpdate) update;
            if (!newMessageUpdate.getMessage().getRoomSecret().equals(roomSecret)) return;

            RoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
            room.setUpdatedRead(true);
            DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RecyclerView recyclerView = findViewById(R.id.recycler_view);

                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            messageList.add(0, newMessageUpdate.getMessage());
                            roomMessagesAdapter.notifyItemInserted(0);
                            int lastVisiblePos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                            if (lastVisiblePos < 3) {
                                recyclerView.scrollToPosition(0);
                            }
                        }
                    });
                }
            });
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            loadData();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loadMembers();
                }
            });
            thread.start();
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;

            MessageReadUpdate readUpdate = (MessageReadUpdate) update;

            if (((MessageReadUpdate) update).getUserId().equals(selfId)) return;
            Message thisMessage = null;
            int index = 0;

            for (int i = 0; i < messageList.size(); i++) {
                Message message = messageList.get(i);
                if (message.getId().equals(readUpdate.getMessageId())) {
                    thisMessage = message;
                    index = i;
                    break;
                }
            }
            if (thisMessage == null) return;

            thisMessage.getMessageReadingList().add(new MessageReading(readUpdate.getMessageId(),
                    readUpdate.getReadTime()));


            int finalIndex = index;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roomMessagesAdapter.notifyItemChanged(finalIndex);
                }
            });

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
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);

                    }
                    imageView.setVisibility(View.VISIBLE);
                    //  UpdateProcessor.getInstance(getApplicationContext()).reconnectSockets();
                } else {
                    findViewById(R.id.status_light).setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
       Thread updateRoom = new Thread(() -> {

                   Room room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
                   if(roomMessagesAdapter == null) return;
                   roomMessagesAdapter.setRoom(room);

                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           loadData();
                       }
                   });

                   loadMembers();
       });
       updateRoom.start();

    }

    private void setupMessageTextInputListener() {
        EditText editText = findViewById(R.id.message_text_input);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userStatus == null) {
                    sendStatusRequest();
                } else if (userStatus.getUserStatus() != UserStatus.TYPING) {
                    sendStatusRequest();
                } else if (userStatus.getTime() - System.currentTimeMillis() > Status.REQUEST_DELAY) {
                    sendStatusRequest();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void sendStatusRequest() {
        Thread statusRequestThread = new Thread(() -> {
            SendUserStatusRequest request = new SendUserStatusRequest(
                    new Status(UserStatus.TYPING, selfId, roomSecret));
            try {
                UpdateProcessor.getInstance().sendRequest(request, roomSecret);
            } catch (UnablePerformRequestException e) {
                e.printStackTrace();
            }
        });
        statusRequestThread.start();
    }

    public void updateUserStatus(String roomSecret, ArrayList<Status> usersStatusList) {
        if (!roomSecret.equals(this.roomSecret)) return;
        TextView membersCount = findViewById(R.id.members_count);

        int size = usersStatusList.size();
        String text;

        if (size == 0) {
            membersCount.setTextColor(theme.getColorTheme().getTextColor());
            text = getString(R.string.members_count).replace("%s",
                    String.valueOf(roomMessagesAdapter.getMembers().size() + 1));
        } else {
            membersCount.setTextColor(theme.getColorTheme().getAccentColor());

            int count = 3;
            if (size < count) count = size;

            String nickNames = "";
            for (int i = 0; i < count; i++) {
                Member member = roomMessagesAdapter.getMembers().get(usersStatusList.get(i).getUserId());
                if (member == null) continue;

                if (i != 0) nickNames = nickNames + ", ";

                nickNames = nickNames + member.getNickname();
            }

            if (size == 1) {
                text = getString(R.string.user_status_typing);
            } else {
                text = getString(R.string.users_status_typing);
            }
            text = text.replace("%s", nickNames);
        }

        String finalText = text;
        runOnUiThread(() -> {
            membersCount.setText(finalText);
        });
    }

    private void applyColorTheme() {
        ImageView button_back = findViewById(R.id.button_back);
        button_back.setColorFilter(theme.getColorTheme().getAccentColor());

        ImageView sendButton = findViewById(R.id.send_button);
        sendButton.getBackground().setTint(theme.getColorTheme().getAccentColor());
        sendButton.setColorFilter(theme.getColorTheme().getSendButtonColor());

        ImageView backgroundView = findViewById(R.id.room_background);
        theme.getChatBackground().applyBackground(backgroundView);
    }
}