package com.diraapp.ui.activities.room;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.databinding.ActivityRoomBinding;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.notifications.Notifier;
import com.diraapp.storage.FileClassifier;
import com.diraapp.storage.images.FilesUploader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.ImageSendActivity;
import com.diraapp.ui.activities.RoomInfoActivity;
import com.diraapp.ui.activities.RoomSelectorActivity;
import com.diraapp.ui.activities.resizer.FluidContentResizer;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messages.RoomMessagesAdapter;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.ui.components.RecordComponentsController;
import com.diraapp.userstatus.UserStatus;
import com.diraapp.userstatus.UserStatusHandler;
import com.diraapp.userstatus.UserStatusListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;


public class RoomActivity extends DiraActivity
        implements RoomActivityContract.View, ProcessorListener, UserStatusListener, RecordComponentsController.RecordListener {
    private String roomSecret;
    private RoomMessagesAdapter roomMessagesAdapter;
    private FilePickerBottomSheet filePickerBottomSheet;
    private ActivityRoomBinding binding;

    private AppTheme theme;
    private RecordComponentsController recordComponentsController;
    private RoomActivityContract.Presenter presenter;

    public static void putRoomExtrasInIntent(Intent intent, String roomSecret, String roomName) {
        intent.putExtra(RoomSelectorActivity.PENDING_ROOM_SECRET, roomSecret);
        intent.putExtra(RoomSelectorActivity.PENDING_ROOM_NAME, roomName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        roomSecret = getIntent().getExtras().getString(RoomSelectorActivity.PENDING_ROOM_SECRET);
        String roomName = getIntent().getExtras().getString(RoomSelectorActivity.PENDING_ROOM_NAME);

        presenter = new RoomActivityPresenter(roomSecret, getCacheUtils().getString(CacheUtils.ID));
        presenter.attachView(this);

        TextView nameView = findViewById(R.id.room_name);
        nameView.setText(roomName);


        Drawable drawable = binding.userStatusAnimation.getDrawable();
        if (drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }
        UpdateProcessor.getInstance().addProcessorListener(this);

        recordComponentsController = new RecordComponentsController(binding.recordButton,
                binding.recordRipple, this,
                binding.camera, binding.bubbleRecordingLayout, binding.bubbleFrame);

        recordComponentsController.setRecordListener(this);

        theme = AppTheme.getInstance();
        applyColorTheme();

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.roomInfoPan.setOnClickListener(new View.OnClickListener() {
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
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.message_text_input);
                if (presenter.sendTextMessage(editText.getText().toString())) {
                    editText.setText("");
                }
            }
        });


        binding.attachButton.setOnClickListener(new View.OnClickListener() {
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


        UserStatusHandler.getInstance().addListener(this);

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
                String fileUri = data.getStringExtra("uri");


                try {
                    if (FileClassifier.isVideoFile(fileUri)) {
                        presenter.uploadAttachmentAndSendMessage(AttachmentType.VIDEO, fileUri, messageText);
                    } else {
                        presenter.uploadAttachmentAndSendMessage(AttachmentType.IMAGE, fileUri, messageText);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        roomMessagesAdapter.release();
        presenter.detachView();
        UpdateProcessor.getInstance().removeProcessorListener(this);
        UserStatusHandler.getInstance().removeListener(this);
    }


    @Override
    public void onSocketsCountChange(float percentOpened) {
        runOnUiThread(() -> {
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.initRoomInfo();
    }

    private void setupMessageTextInputListener() {
        EditText editText = findViewById(R.id.message_text_input);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recordComponentsController.handleInputAnimation(
                        editText.getText().length() == 0,
                        binding.sendButton);
                if (count == 0) {

                    return;
                }
                presenter.sendStatus(UserStatusType.TYPING);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void updateUserStatus(String roomSecret, ArrayList<UserStatus> usersUserStatusList) {
        runOnUiThread(() -> {
        String userId = getCacheUtils().getString(CacheUtils.ID);
        if (!roomSecret.equals(this.roomSecret)) return;
        TextView membersCount = findViewById(R.id.members_count);

        for (UserStatus userStatus : new ArrayList<>(usersUserStatusList)) {
            if (userStatus.getUserId().equals(userId)) usersUserStatusList.remove(userStatus);
        }

        int size = usersUserStatusList.size();
        String text;

        if (size == 0) {
            binding.userStatusAnimation.setVisibility(View.GONE);
            membersCount.setTextColor(ContextCompat.getColor(this, R.color.medium_light_light_gray));
            text = getString(R.string.members_count).replace("%s",
                    String.valueOf(roomMessagesAdapter.getMembers().size() + 1));
        } else {
            membersCount.setTextColor(theme.getColorTheme().getAccentColor());

            binding.userStatusAnimation.setVisibility(View.VISIBLE);
            StringBuilder nickNames = new StringBuilder();
            for (int i = 0; i < size; i++) {
                Member member = roomMessagesAdapter.getMembers().get(usersUserStatusList.get(i).getUserId());
                if (member == null) continue;
                if (member.getId().equals(userId)) continue;

                if (i != 0) nickNames.append(", ");

                nickNames.append(member.getNickname());
            }

            if (nickNames.length() > 22) {
                nickNames = new StringBuilder(nickNames.substring(0, 22) + "..");
            }

            if (size == 1) {
                text = getString(R.string.user_status_typing);
            } else {
                text = getString(R.string.users_status_typing);
            }

            text = text.replace("%s", nickNames.toString());
        }

        String finalText = text;

            membersCount.setText(finalText);
        });
    }

    private void applyColorTheme() {
        ImageView buttonBack = findViewById(R.id.button_back);
        buttonBack.setColorFilter(theme.getColorTheme().getAccentColor());

        ImageView sendButton = findViewById(R.id.send_button);
        sendButton.getBackground().setColorFilter(theme.getColorTheme().getAccentColor(), PorterDuff.Mode.SRC_IN);
        sendButton.setColorFilter(theme.getColorTheme().getIconButtonColor());

        ImageView backgroundView = findViewById(R.id.room_background);
        theme.getChatBackground().applyBackground(backgroundView);
    }

    @Override
    public void onMediaMessageRecorded(String path, AttachmentType attachmentType) {
        presenter.uploadAttachmentAndSendMessage(attachmentType, path, "");
    }

    @Override
    public void fillRoomInfo(Bitmap picture, Room room) {
        runOnUiThread(() -> {
            if (picture != null) {
                binding.roomPicture.setImageBitmap(picture);
            }

            TextView roomName = findViewById(R.id.room_name);
            applyColorTheme();
            roomName.setText(room.getName());
            if (roomMessagesAdapter != null) return;
            roomMessagesAdapter = new RoomMessagesAdapter(RoomActivity.this, binding.recyclerView, roomSecret, room.getServerAddress(), room, new RoomMessagesAdapter.MessageAdapterListener() {
                @Override
                public void onFirstItemScrolled(Message message, int index) {
                    presenter.loadMessagesBefore(message, index);
                }
            });

            binding.recyclerView.setAdapter(roomMessagesAdapter);

            presenter.loadMessages();
        });
    }

    @Override
    public void notifyRecyclerMessage() {
        binding.recyclerView.post(new Runnable() {
            @Override
            public void run() {

                roomMessagesAdapter.notifyItemInserted(0);
                int lastVisiblePos = ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (lastVisiblePos < 3) {
                    binding.recyclerView.scrollToPosition(0);
                }
            }
        });
    }

    @Override
    public void notifyMessagesChanged() {
        runOnUiThread(() -> roomMessagesAdapter.notifyDataSetChanged());
    }

    @Override
    public void notifyAdapterChanged(int index) {
        runOnUiThread(() -> roomMessagesAdapter.notifyItemChanged(index));
    }

    @Override
    public void setMembers(HashMap<String, Member> members) {
        runOnUiThread(() -> updateUserStatus(roomSecret, new ArrayList<>()));
        roomMessagesAdapter.setMembers(members);
    }

    @Override
    public void setRoom(Room room) {
        roomMessagesAdapter.setRoom(room);
    }

    @Override
    public void setMessages(List<Message> messages) {
        if (roomMessagesAdapter == null) return;
        roomMessagesAdapter.setMessages(messages);
    }

    @Override
    public void uploadFile(String sourceFileUri, Callback callback, boolean deleteAfterUpload, String serverAddress, String encryptionKey) {
        try {
            FilesUploader.uploadFile(sourceFileUri, callback, getApplicationContext(), deleteAfterUpload, serverAddress, encryptionKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void compressVideo(List<Uri> urisToCompress, String fileUri, VideoQuality videoQuality, Double videoHeight,
                              Double videoWidth, RoomActivityPresenter.RoomAttachmentCallback callback, String serverAddress, String encryptionKey) {
        VideoCompressor.start(getApplicationContext(), urisToCompress,
                false,
                null,
                new AppSpecificStorageConfiguration(
                        new File(fileUri).getName() + "temp_compressed", null), // => required name
                new Configuration(videoQuality,
                        false,
                        2,
                        false,
                        false,
                        videoHeight,
                        videoWidth), new CompressionListener() {
                    @Override
                    public void onStart(int i) {

                    }

                    @Override
                    public void onSuccess(int i, long l, @Nullable String path) {
                        if (path != null) {
                            try {
                                FilesUploader.uploadFile(path,
                                        callback.setFileUri(path),
                                        getApplicationContext(), true,
                                        serverAddress, encryptionKey);
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
    }

    @Override
    public DiraRoomDatabase getRoomDatabase() {
        return DiraRoomDatabase.getDatabase(getApplicationContext());
    }

    @Override
    public DiraMessageDatabase getMessagesDatabase() {
        return DiraMessageDatabase.getDatabase(getApplicationContext());
    }


}