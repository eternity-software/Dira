package com.diraapp.ui.activities.room;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.diraapp.DiraApplication;
import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.databinding.ActivityRoomBinding;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.notifications.Notifier;
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.images.FilesUploader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.ImageSendActivity;
import com.diraapp.ui.activities.RoomInfoActivity;
import com.diraapp.ui.activities.RoomSelectorActivity;
import com.diraapp.ui.activities.resizer.FluidContentResizer;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messages.MessageReplyClickedListener;
import com.diraapp.ui.adapters.messages.RoomMessagesAdapter;
import com.diraapp.ui.appearance.BackgroundType;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.ui.components.RecordComponentsController;
import com.diraapp.ui.components.viewswiper.ViewSwiper;
import com.diraapp.ui.components.viewswiper.ViewSwiperListener;
import com.diraapp.userstatus.UserStatus;
import com.diraapp.userstatus.UserStatusHandler;
import com.diraapp.userstatus.UserStatusListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.SliderActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;


public class RoomActivity extends DiraActivity
        implements RoomActivityContract.View, ProcessorListener, UserStatusListener, RecordComponentsController.RecordListener {

    private static final int DO_NOT_NEED_TO_SCROLL = -1;

    private static final int IS_ROOM_OPENING = -1;
    private String roomSecret;
    private RoomMessagesAdapter roomMessagesAdapter;
    private FilePickerBottomSheet filePickerBottomSheet;
    private ActivityRoomBinding binding;
    private RecordComponentsController recordComponentsController;
    private RoomActivityContract.Presenter presenter;

    private int lastVisiblePosition = 0;
    private final MediaGridItemListener mediaGridItemListener = new MediaGridItemListener() {
        @Override
        public void onItemClick(int pos, final View view) {
            ImageSendActivity.open(RoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                    (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_MESSAGE);
        }

        @Override
        public void onLastItemLoaded(int pos, View view) {

        }
    };

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

        //binding.recyclerView.setItemViewCacheSize(50);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(false);

        ViewSwiper viewSwiper = new ViewSwiper(binding.recyclerView);
        viewSwiper.setViewSwiperListener((ViewSwiperListener) presenter);

//        binding.recyclerView.getRecycledViewPool().setMaxRecycledViews(1, 4);
//        binding.recyclerView.getRecycledViewPool().setMaxRecycledViews(21, 4);

        TextView nameView = findViewById(R.id.room_name);
        nameView.setText(roomName);

        setBackground();

        Drawable drawable = binding.userStatusAnimation.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
        UpdateProcessor.getInstance().addProcessorListener(this);

        recordComponentsController = new RecordComponentsController(binding.recordButton,
                binding.recordRipple, this,
                binding.camera, binding.bubbleRecordingLayout, binding.bubbleFrame);


        recordComponentsController.setRecordListener(this);

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
        filePickerBottomSheet = new FilePickerBottomSheet();
        filePickerBottomSheet.setRunnable(mediaGridItemListener);

        binding.attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filePickerBottomSheet.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        onResume();
                    }
                });


                presenter.sendStatus(UserStatusType.PICKING_FILE);

                filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
                onPause();
            }
        });

        Notifier.cancelAllNotifications(getApplicationContext());

        FluidContentResizer fluidContentResizer = new FluidContentResizer();
        fluidContentResizer.listen(this);


        UserStatusHandler.getInstance().addListener(this);

        binding.replyClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                performHeightAnimation( Numbers.dpToPx(48, RoomActivity.this), 0, binding.replyLayout)
                        .addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(@NonNull Animator animator) {
                                setReplyMessage(null);
                            }

                            @Override
                            public void onAnimationCancel(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(@NonNull Animator animator) {

                            }
                        });

            }
        });

    }

    private void setBackground() {
        ImageView backgroundView = findViewById(R.id.room_background);
        CacheUtils cacheUtils = getCacheUtils();

        if (!cacheUtils.hasKey(CacheUtils.BACKGROUND)) {
            cacheUtils.setString(CacheUtils.BACKGROUND, BackgroundType.LOVE.toString());
        }
        if (cacheUtils.hasKey(CacheUtils.BACKGROUND_PATH)) {
            ImageViewCompat.setImageTintList(backgroundView, null);
            backgroundView.setImageBitmap(AppStorage.getBitmapFromPath(
                    cacheUtils.getString(CacheUtils.BACKGROUND_PATH)));
            return;
        }
        int drawableResourceId = this.getResources().getIdentifier(
                "background_" + cacheUtils.getString(CacheUtils.BACKGROUND).toLowerCase(),
                "drawable", this.getPackageName());
        backgroundView.setColorFilter(Theme.getColor(this, R.color.gray));
        backgroundView.setImageDrawable(getDrawable(drawableResourceId));

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
                    presenter.sendStatus(UserStatusType.SENDING_FILE);
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
                    imageView.setColorFilter(Theme.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    imageView.setColorFilter(Theme.getColor(getApplicationContext(), R.color.yellow), android.graphics.PorterDuff.Mode.SRC_IN);

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
        filePickerBottomSheet.setRunnable(mediaGridItemListener);
        presenter.initRoomInfo();

        if (lastVisiblePosition != 0) {
            binding.recyclerView.scrollToPosition(lastVisiblePosition);
            lastVisiblePosition = 0;
        }
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
            try {

                String userId = getCacheUtils().getString(CacheUtils.ID);
                if (!roomSecret.equals(this.roomSecret)) return;
                TextView membersCount = findViewById(R.id.members_count);

                for (UserStatus userStatus : new ArrayList<>(usersUserStatusList)) {
                    if (userStatus.getUserId().equals(userId))
                        usersUserStatusList.remove(userStatus);
                }

                int size = usersUserStatusList.size();
                String text = "";

                if (size == 0) {
                    binding.userStatusAnimation.setVisibility(View.GONE);
                    membersCount.setTextColor(Theme.getColor(this, R.color.subtitle_color));
                    text = getString(R.string.members_count).replace("%s",
                            String.valueOf(roomMessagesAdapter.getMembers().size() + 1));
                } else {
                    membersCount.setTextColor(Theme.getColor(this, R.color.subtitle_color_accent));

                    ArrayList<UserStatus> bubbleStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> pickingFileStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> voiceStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> writingStatuses = new ArrayList<>(size);

                    binding.userStatusAnimation.setVisibility(View.VISIBLE);
                    for (int i = 0; i < size; i++) {
                        UserStatus status = usersUserStatusList.get(i);
                        Member member = roomMessagesAdapter.getMembers().get(status.getUserId());
                        if (member == null) continue;
                        if (member.getId().equals(userId)) continue;

                        if (status.getUserStatus() == UserStatusType.TYPING) {
                            writingStatuses.add(status);
                        } else if (status.getUserStatus() == UserStatusType.PICKING_FILE) {
                            pickingFileStatuses.add(status);
                        } else if (status.getUserStatus() == UserStatusType.RECORDING_VOICE) {
                            voiceStatuses.add(status);
                        } else if (status.getUserStatus() == UserStatusType.RECORDING_BUBBLE) {
                            bubbleStatuses.add(status);
                        }
                    }

                    ArrayList<UserStatus> statuses = new ArrayList<>(size);

                    if (bubbleStatuses.size() > 0) {
                        statuses = bubbleStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_bubble);
                        } else {
                            text = getString(R.string.users_status_bubble);
                        }
                    } else if (voiceStatuses.size() > 0) {
                        statuses = voiceStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_voice);
                        } else {
                            text = getString(R.string.users_status_voice);
                        }
                    } else if (pickingFileStatuses.size() > 0) {
                        statuses = pickingFileStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_file);
                        } else {
                            text = getString(R.string.users_status_file);
                        }
                    } else if (writingStatuses.size() > 0) {
                        statuses = writingStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_typing);
                        } else {
                            text = getString(R.string.users_status_typing);
                        }
                    }

                    StringBuilder nickNames = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        UserStatus status = statuses.get(i);
                        Member member = roomMessagesAdapter.getMembers().get(status.getUserId());
                        if (i != 0) nickNames.append(", ");

                        nickNames.append(member.getNickname());
                    }

                    if (nickNames.length() > 22) {
                        nickNames = new StringBuilder(nickNames.substring(0, 22) + "..");
                    }

                    text = text.replace("%s", nickNames.toString());
                }

                String finalText = text;

                membersCount.setText(finalText);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void setReplyMessage(Message message) {
        runOnUiThread(() -> {
            if (message == null) {
                presenter.setReplyingMessage(null);
                binding.replyLayout.setVisibility(View.GONE);
                return;
            }

            showKeyboard(binding.messageTextInput);

            String text = "";
            Attachment attachment = null;
            boolean showImage = false;
            int size = message.getAttachments().size();
            if (size > 0) {
                attachment = message.getAttachments().get(0);
            }

            if (attachment != null) {
                binding.replyText.setTextColor(Theme.getColor(this, R.color.self_reply_color));
                if (size > 1) {
                    text = getResources().getString(R.string.message_type_attachments);
                } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                    text = getResources().getString(R.string.message_type_bubble);
                } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                    text = getResources().getString(R.string.message_type_video);
                } else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                    text = getResources().getString(R.string.message_type_voice);
                } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                    text = message.getText();
                    if (text == null | "".equals(text)) {
                        text = getResources().getString(R.string.message_type_image);
                    } else {
                        binding.replyText.setTextColor(Theme.getColor
                                (this, R.color.self_message_color));
                    }

                    File file = AttachmentsStorage.getFileFromAttachment(attachment,
                            this, roomSecret);

                    if (file != null) {
                        Picasso.get().load(file).into(binding.replyImage);
                        binding.replyImageCard.setVisibility(View.VISIBLE);
                    }
                    showImage = true;
                }
            } else {
                text = message.getText();
                if (text == null) text = "";
            }

            if (!showImage) {
                binding.replyImageCard.setVisibility(View.GONE);
            }

            HashMap<String, Member> members = ((RoomMessagesAdapter) binding.recyclerView.getAdapter())
                    .getMembers();

            String author = "";
            boolean isUnknown = true;
            if (message.getAuthorId().equals(getCacheUtils().getString(CacheUtils.ID))) {
                author = getString(R.string.you);
                isUnknown = false;
            } else if (members != null) {
                Member member = members.get(message.getAuthorId());
                if (member != null) {
                    author = member.getNickname();
                    isUnknown = false;
                }
            }

            if (isUnknown) {
                author = getString(R.string.unknown);
            }

            binding.replyAuthorName.setText(author);
            binding.replyText.setText(text);



            if(binding.replyLayout.getVisibility() != View.VISIBLE)
            {
                binding.replyLayout.setVisibility(View.VISIBLE);
                performHeightAnimation(0, Numbers.dpToPx(48, this), binding.replyLayout);
            }

        });
    }

    @Override
    public void smoothScrollTo(int position) {
        binding.recyclerView.smoothScrollToPosition(position);
    }

    @Override
    public Bitmap getBitmap(String path) {
        return AppStorage.getBitmapFromPath(path, this);
    }

    @Override
    public void onMediaMessageRecorded(String path, AttachmentType attachmentType) {
        presenter.uploadAttachmentAndSendMessage(attachmentType, path, "");
    }

    @Override
    public void onMediaMessageRecordingStart(AttachmentType attachmentType) {
        if (attachmentType == AttachmentType.VOICE) {
            presenter.sendStatus(UserStatusType.RECORDING_VOICE);
            return;
        }
        presenter.sendStatus(UserStatusType.RECORDING_BUBBLE);
    }

    public void showKeyboard(final EditText ettext){
        ettext.requestFocus();
        ettext.postDelayed(new Runnable(){
                               @Override public void run(){
                                   InputMethodManager keyboard=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                   keyboard.showSoftInput(ettext,0);
                               }
                           }
                ,100);
    }

    @Override
    public void fillRoomInfo(Bitmap picture, Room room) {
        runOnUiThread(() -> {
            if (picture != null) {
                binding.roomPicture.setImageBitmap(picture);
            }

            TextView roomName = findViewById(R.id.room_name);
            roomName.setText(room.getName());
            if (roomMessagesAdapter != null) return;
            roomMessagesAdapter = new RoomMessagesAdapter(RoomActivity.this, binding.recyclerView, room.getServerAddress(), room, new RoomMessagesAdapter.MessageAdapterListener() {
                @Override
                public void onFirstItemScrolled(Message message, int index) {
                    presenter.loadMessagesBefore(message, index);
                }

                @Override
                public void onLastLoadedScrolled(Message message, int index) {
                    presenter.loadNewerMessage(message, index);
                }
            });

            binding.recyclerView.setAdapter(roomMessagesAdapter);
            roomMessagesAdapter.setReplyClickedListener((MessageReplyClickedListener) presenter);

            presenter.loadMessages();
        });
    }

    @Override
    public void notifyRecyclerMessage(Message message, boolean needUpdateList) {
        if (message.getAuthorId() != null) {
            if (!message.getAuthorId().equals(roomMessagesAdapter.getSelfId())) {
                roomMessagesAdapter.getRoom().addNewUnreadMessageId(message.getId());
            }
        }
        binding.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (needUpdateList) {
                    roomMessagesAdapter.notifyItemInserted(0);
                    int lastVisiblePos = ((LinearLayoutManager)
                            binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                    if (DiraApplication.isBackgrounded()) {
                        if (lastVisiblePosition == 0) {
                            lastVisiblePosition = lastVisiblePos;
                        }
                        lastVisiblePosition++;
                    } else if (lastVisiblePos < 3) {
                        binding.recyclerView.scrollToPosition(0);
                    }
                }
            }
        });
    }

    @Override
    public void notifyOnRoomOpenMessagesLoaded(int scrollPosition) {
        if (scrollPosition == 0) {
            notifyMessagesChanged(IS_ROOM_OPENING, 0, DO_NOT_NEED_TO_SCROLL);
            return;
        }
        notifyMessagesChanged(IS_ROOM_OPENING, 0, scrollPosition);
    }

    @Override
    public void notifyMessageChangedWithoutScroll(int start, int last) {
        notifyMessagesChanged(start, last, DO_NOT_NEED_TO_SCROLL);
    }

    @Override
    public void notifyMessagesChanged(int start, int last, int scrollPosition) {
        runOnUiThread(() -> {
            if (start == IS_ROOM_OPENING) {
                roomMessagesAdapter.notifyDataSetChanged();
            } else {
                roomMessagesAdapter.notifyItemRangeInserted(start, last);
            }

            if (scrollPosition != DO_NOT_NEED_TO_SCROLL) {
                if (start == IS_ROOM_OPENING) {
                    ((LinearLayoutManager) binding.recyclerView.getLayoutManager())
                            .scrollToPositionWithOffset(scrollPosition, 20);
                    return;
                }
                binding.recyclerView.scrollToPosition(scrollPosition);
            }
        });
    }

    @Override
    public void notifyAdapterItemChanged(int index) {
        runOnUiThread(() -> roomMessagesAdapter.notifyItemChanged(index));
    }

    @Override
    public void notifyAdapterItemsDeleted(int start, int count) {
        runOnUiThread(() -> {
            roomMessagesAdapter.notifyItemRangeRemoved(start, count);
        });
        Logger.logDebug(this.getClass().getSimpleName(),
                "Deleted items from start - " + start + " to " + (count - 1) +
                        " size is " + roomMessagesAdapter.getItemCount());

    }

    @Override
    public void setMembers(HashMap<String, Member> members) {
        if (roomMessagesAdapter == null) return;
        roomMessagesAdapter.setMembers(members);
        runOnUiThread(() -> updateUserStatus(roomSecret, new ArrayList<>()));

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
                              Double videoWidth, RoomActivityPresenter.RoomAttachmentCallback callback, String serverAddress, String encryptionKey, int
                              bitrate) {
        VideoCompressor.start(getApplicationContext(), urisToCompress,
                true,
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
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression fail: " + s);
                    }

                    @Override
                    public void onProgress(int i, float v) {
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression progress: " + i + " " + v);
                    }

                    @Override
                    public void onCancelled(int i) {
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression cancelled: " + i);
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