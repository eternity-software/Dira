package com.diraapp.ui.activities.room;

import static com.diraapp.ui.activities.roominfo.RoomInfoActivity.MESSAGE_TO_SCROLL_ID;
import static com.diraapp.ui.activities.roominfo.RoomInfoActivity.MESSAGE_TO_SCROLL_TIME;

import android.Manifest;
import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.BuildConfig;
import com.diraapp.DiraApplication;
import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.requests.Request;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.userstatus.UserStatusHandler;
import com.diraapp.api.userstatus.UserStatusListener;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.databinding.ActivityRoomBinding;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.notifications.Notifier;
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.MediaPreviewActivity;
import com.diraapp.ui.activities.MediaSendActivity;
import com.diraapp.ui.activities.PreparedActivity;
import com.diraapp.ui.activities.roominfo.RoomInfoActivity;
import com.diraapp.ui.activities.resizer.FluidContentResizer;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.MessagesAdapter;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.adapters.messages.views.BalloonMessageMenu;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.RoomViewHolderFactory;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.ListenableViewHolder;
import com.diraapp.ui.appearance.BackgroundType;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;
import com.diraapp.ui.components.GlobalPlayerComponent;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.ui.components.RecordComponentsController;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.viewswiper.ViewSwiper;
import com.diraapp.ui.components.viewswiper.ViewSwiperListener;
import com.diraapp.ui.fragments.navigation.selector.RoomSelectorFragment;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayerListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.SliderActivity;
import com.diraapp.utils.android.DeviceUtils;
import com.r0adkll.slidr.model.SlidrInterface;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class RoomActivity extends DiraActivity
        implements RoomActivityContract.View, ProcessorListener, UserStatusListener,
        RecordComponentsController.RecordListener, MessageAdapterContract,
        FilePickerBottomSheet.MultiFilesListener, BalloonMessageMenu.BalloonMenuListener,
        GlobalMediaPlayerListener {

    public static final int SEND_FILE_CODE = 34234;

    private static final int DO_NOT_NEED_TO_SCROLL = -1;

    private static final int IS_ROOM_OPENING = -1;

    private final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
    private final HashSet<String> messagesToBlinkIds = new HashSet<>();
    private String roomSecret;
    private MessagesAdapter messagesAdapter;
    private FilePickerBottomSheet filePickerBottomSheet;
    private ActivityRoomBinding binding;
    private final MediaGridItemListener mediaGridItemListener = new MediaGridItemListener() {
        @Override
        public void onItemClick(int pos, final View view) {
            MediaSendActivity.open(RoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(),
                    binding.messageTextInput.getText().toString(),
                    (MediaGridItem) view, MediaSendActivity.IMAGE_PURPOSE_MESSAGE);

        /*    PreviewActivity.open(RoomActivity.this,  filePickerBottomSheet.getMedia().get(pos).getFilePath(),
                    AppStorage.getBitmapFromPath(filePickerBottomSheet.getMedia().get(pos).getFilePath()), false, view);*/
        }

        @Override
        public void onLastItemLoaded(int pos, View view) {

        }
    };
    private boolean isArrowShowed = true;
    private boolean isScrollIndicatorShown = true;
    private boolean isLastPinnedShown = false;
    private int currentPinnedShownPos = 0;
    private int currentPinnedTapCount = 0;
    private RecordComponentsController recordComponentsController;
    private RoomActivityContract.Presenter presenter;
    private int lastVisiblePosition = 0;
    private ViewSwiper viewSwiper;

    private ListenableViewHolder currentListenableViewHolder;

    private boolean isMediaPreviewActivityOpened = false;


    public static void putRoomExtrasInIntent(Intent intent, String roomSecret, String roomName) {
        intent.putExtra(RoomSelectorFragment.PENDING_ROOM_SECRET, roomSecret);
        intent.putExtra(RoomSelectorFragment.PENDING_ROOM_NAME, roomName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SliderActivity sliderActivity = new SliderActivity();
        SlidrInterface slidrInterface = sliderActivity.attachSlider(this);

        roomSecret = getIntent().getExtras().getString(RoomSelectorFragment.PENDING_ROOM_SECRET);
        String roomName = getIntent().getExtras().getString(RoomSelectorFragment.PENDING_ROOM_NAME);

        presenter = new RoomActivityPresenter(roomSecret, getCacheUtils().getString(CacheUtils.ID));
        presenter.attachView(this);

        ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).setInitialPrefetchItemCount(100);

        binding.recyclerView.setRecycledViewPool(recycledViewPool);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setItemViewCacheSize(2);

        binding.recyclerView.setItemAnimator(new MessageItemAnimator());
        viewSwiper = new ViewSwiper(binding.recyclerView);
        viewSwiper.setViewSwiperListener((ViewSwiperListener) presenter);

//        binding.recyclerView.getRecycledViewPool().setMaxRecycledViews(1, 4);
//        binding.recyclerView.getRecycledViewPool().setMaxRecycledViews(21, 4);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        GlobalMediaPlayer.getInstance().registerListener(this);


        TextView nameView = findViewById(R.id.room_name);
        nameView.setText(roomName);

        setBackground();

        Drawable drawable = binding.userStatusAnimation.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
        UpdateProcessor.getInstance().addProcessorListener(this);

        recordComponentsController = new RecordComponentsController(binding.recordButton,
                binding.recordRipple, binding.recordingText, binding.recordingTip, binding.recordingIndicator,
                this, binding.recordingStatusBar,
                binding.camera, slidrInterface, binding.bubbleRecordingLayout, binding.bubbleFrame);


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

                startActivityForResult(intent, RoomInfoActivity.RESULT_CODE_SCROLL_TO_MESSAGE);
            }
        });


        setupMessageTextInputListener();
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText messageInput = binding.messageTextInput;

                if (presenter.sendTextMessage(messageInput.getText().toString())) {
                    messageInput.setText("");
                }
            }
        });


        binding.attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePickerBottomSheet = new FilePickerBottomSheet();
                filePickerBottomSheet.setMultiSelection(true);
                filePickerBottomSheet.setMultiFilesListener(RoomActivity.this);

                filePickerBottomSheet.setRunnable(mediaGridItemListener);
                filePickerBottomSheet.setOnDismiss(new Runnable() {
                    @Override
                    public void run() {
                        onResume();
                    }
                });

                presenter.sendStatus(UserStatusType.PICKING_FILE);
                filePickerBottomSheet.setMessageText(binding.messageTextInput.getText().toString());
                filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
                onPause();
            }
        });

        Notifier.cancelAllNotifications(getApplicationContext());

        FluidContentResizer fluidContentResizer = new FluidContentResizer();
        fluidContentResizer.listen(this);


        UserStatusHandler.getInstance().addListener(this);

        binding.replyClose.setOnClickListener((View view) -> setReplyMessage(null));

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
        try {
            int drawableResourceId = this.getResources().getIdentifier(
                    "background_" + cacheUtils.getString(CacheUtils.BACKGROUND).toLowerCase(),
                    "drawable", this.getPackageName());
            backgroundView.setColorFilter(Theme.getColor(this, R.color.gray));
            backgroundView.setImageDrawable(getDrawable(drawableResourceId));
        } catch (Exception e) {
            backgroundView.setImageBitmap(null);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode != RESULT_OK && resultCode != MediaSendActivity.CODE) {
                return;
            }

            if (requestCode == 2) {
                final Bundle extras = data.getExtras();
                if (extras != null) {

                }
            } else if (requestCode == SEND_FILE_CODE) {
                Logger.logDebug(this.getClass().getSimpleName(), "On result: Sending file");

                binding.messageTextInput.setText("");
                String messageText = data.getStringExtra("text");

                ArrayList<Uri> uriList = new ArrayList<>();

                boolean isSingleFile = data.getData() != null;
                if (isSingleFile) {
                    Uri uri = data.getData();
                    uriList.add(uri);

                    presenter.sendFileAttachmentMessage(uriList, messageText, this);
                    return;
                }

                boolean isMultiple = data.getClipData() != null;
                if (!isMultiple) return;

                ClipData clipData = data.getClipData();
                final int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    uriList.add(clipData.getItemAt(i).getUri());
                }

                presenter.sendFileAttachmentMessage(uriList, messageText, this);

            } else if (requestCode == RoomInfoActivity.RESULT_CODE_SCROLL_TO_MESSAGE) {
                final Bundle extras = data.getExtras();

                if (extras != null) {
                    long messageTime = extras.getLong(MESSAGE_TO_SCROLL_TIME);
                    String messageId = extras.getString(MESSAGE_TO_SCROLL_ID);

                    if (messageTime != 0L) {
                        presenter.scrollToMessage(messageId, messageTime);
                    }
                }
            }

            if (resultCode == MediaSendActivity.CODE) {
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
                binding.messageTextInput.setText("");
                List<String> fileUris = data.getExtras().getStringArrayList("uris");

                try {
                    ArrayList<Attachment> attachments = new ArrayList<>();
                    presenter.sendStatus(UserStatusType.SENDING_FILE);
                    if (fileUris.size() == 1) {
                        final String replyId = presenter.getAndClearReplyId();
                        RoomActivityPresenter.AttachmentReadyListener attachmentReadyListener = attachment -> {
                            attachments.add(attachment);
                            presenter.sendMessage(attachments, messageText, replyId);
                        };
                        String fileUri = fileUris.get(0);
                        if (FileClassifier.isVideoFile(fileUri)) {
                            presenter.uploadAttachment(AttachmentType.VIDEO, attachmentReadyListener, fileUri, this);
                        } else {
                            presenter.uploadAttachment(AttachmentType.IMAGE, attachmentReadyListener, fileUri, this);
                        }
                    } else {
                        System.out.println("WOw! Several attachments!");
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
        // binding.recyclerView.getRecycledViewPool().clear();
        messagesAdapter.release();
        presenter.detachView();
        UpdateProcessor.getInstance().removeProcessorListener(this);
        UserStatusHandler.getInstance().removeListener(this);
        GlobalMediaPlayer.getInstance().removeListener(this);

        GlobalPlayerComponent component = findViewById(R.id.global_player);
        component.release();

        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LinearLayoutManager layoutManager = (LinearLayoutManager)
                binding.recyclerView.getLayoutManager();
        if (layoutManager == null) return;
        int firstVisiblePos = layoutManager.findFirstVisibleItemPosition();

        Message message = presenter.getMessageByPosition(firstVisiblePos);
        if (message != null)
            getRoom().setFirstVisibleScrolledItemId(message.getId());

        getRoom().setUnsentText(binding.messageTextInput.getText().toString());
        presenter.updateDynamicRoomFields();
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

        presenter.initRoomInfo();
        isMediaPreviewActivityOpened = false;

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

        if (BuildConfig.DEBUG) {
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    Logger.logDebug("RoomActivity", "EditText focus changed - " + b);
                }
            });
        }

    }

    @Override
    public void updateRoomStatus(String roomSecret, ArrayList<UserStatus> usersUserStatusList) {
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
                            String.valueOf(presenter.getMembers().size() + 1));
                } else {
                    membersCount.setTextColor(Theme.getColor(this, R.color.subtitle_color_accent));

                    ArrayList<UserStatus> bubbleStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> pickingFileStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> sendingFileStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> voiceStatuses = new ArrayList<>(size);
                    ArrayList<UserStatus> writingStatuses = new ArrayList<>(size);

                    binding.userStatusAnimation.setVisibility(View.VISIBLE);
                    for (int i = 0; i < size; i++) {
                        UserStatus status = usersUserStatusList.get(i);
                        Member member = presenter.getMembers().get(status.getUserId());
                        if (member == null) continue;
                        if (member.getId().equals(userId)) continue;

                        if (status.getUserStatus() == UserStatusType.TYPING) {
                            writingStatuses.add(status);
                        } else if (status.getUserStatus() == UserStatusType.PICKING_FILE) {
                            pickingFileStatuses.add(status);
                        } else if (status.getUserStatus() == UserStatusType.SENDING_FILE) {
                            sendingFileStatuses.add(status);
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
                    } else if (sendingFileStatuses.size() > 0) {
                        statuses = sendingFileStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_sending_file);
                        } else {
                            text = getString(R.string.users_status_sending_file);
                        }
                    } else if (pickingFileStatuses.size() > 0) {
                        statuses = pickingFileStatuses;
                        if (statuses.size() == 1) {
                            text = getString(R.string.user_status_picking_file);
                        } else {
                            text = getString(R.string.users_status_picking_file);
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
                        Member member = presenter.getMembers().get(status.getUserId());
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
            } else {
                showKeyboard(binding.messageTextInput);
            }

            fillViews(message, binding.replyAuthorName, binding.replyText, binding.replyImageCard,
                    binding.replyImage, DeviceUtils.dpToPx(48, this), binding.replyLayout);
        });
    }

    private void fillViews(Message message, TextView authorText, TextView textView,
                           CardView imageCard, ImageView image, int px, LinearLayout layout) {
        if (message == null) {

            if (layout.getVisibility() == View.VISIBLE) {
                performHeightAnimation(px, 0, layout).addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animator) {
                        layout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animator) {
                    }
                });
            }
            return;
        }

        String text = "";
        Attachment attachment = null;
        boolean showImage = false;
        int size = message.getAttachments().size();
        if (size > 0) {
            attachment = message.getSingleAttachment();
        }

        if (message.hasCustomClientData()) {
            text = message.getMessageTextPreview(this);
        } else if (attachment != null) {
            textView.setTextColor(Theme.getColor(this, R.color.self_reply_color));
            if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                text = message.getText();
                if (text == null | "".equals(text)) {
                    text = getResources().getString(R.string.message_type_image);
                } else {
                    textView.setTextColor(Theme.getColor
                            (this, R.color.self_message_color));
                }

                File file = AttachmentDownloader.getFileFromAttachment(attachment,
                        this, roomSecret);

                if (file != null) {
                    Picasso.get().load(file).into(image);
                    imageCard.setVisibility(View.VISIBLE);
                }
                showImage = true;
            } else {
                text = message.getAttachmentText(this);
            }
        } else {
            text = message.getText();
            if (text == null) text = "";
        }

        if (!showImage) {
            imageCard.setVisibility(View.GONE);
        }

        HashMap<String, Member> members = presenter.getMembers();

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

        authorText.setText(author);
        textView.setText(text);


        if (layout.getVisibility() != View.VISIBLE) {
            layout.setVisibility(View.VISIBLE);
            performHeightAnimation(0, px, layout);
        }
        layout.requestLayout();
    }

    @Override
    public void smoothScrollTo(int position) {
        binding.recyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void scrollTo(int index) {
        binding.recyclerView.scrollToPosition(index);
    }

    @Override
    public void scrollToAndStop(int index) {
        binding.recyclerView.scrollToPosition(index);
        binding.recyclerView.stopScroll();
    }

    @Override
    public Bitmap getBitmap(String path) {
        return AppStorage.getBitmapFromPath(path, this);
    }

    @Override
    public void setOnScrollListener() {
        LinearLayout arrow = binding.scrollArrow;

        boolean showArrow = getRoom().getUnreadMessagesIds().size() > 1;

        boolean isMessagesIdsExist = getRoom().getFirstVisibleScrolledItemId() != null &&
                getRoom().getLastMessageId() != null;

        if (!showArrow && isMessagesIdsExist) {
            showArrow = !getRoom().getFirstVisibleScrolledItemId().equals(getRoom().getLastMessageId());
        }

        if (showArrow) {
            isArrowShowed = true;
            arrow.setVisibility(View.VISIBLE);
        } else {
            isArrowShowed = false;
            arrow.setVisibility(View.INVISIBLE);
        }

        updateScrollArrowIndicator();

        arrow.setOnClickListener((View view) -> {
            int lastVisiblePos = ((LinearLayoutManager)
                    binding.recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            presenter.onScrollArrowPressed(lastVisiblePos);
        });

        // Dirty code bellow. Fix is required
        // Note: it is supporting only API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                final int ARROW_APPEARANCE = 20;
                final int ARROW_DISAPPEARANCE = -20;

                @Override
                public void onScrollChange(View view, int scrollX, int scrollY, int oldX, int oldY) {
                    int dy = scrollY - oldY;
                    LinearLayoutManager layoutManager = (LinearLayoutManager)
                            binding.recyclerView.getLayoutManager();
                    if (layoutManager == null) return;
                    int position = layoutManager.findFirstVisibleItemPosition();

                    if (currentPinnedTapCount != 0) currentPinnedTapCount = 0;
//                    if (updatePinnedOnScroll) {
//                        Logger.logDebug("fdfsdfsdf", "last on scroll");
//                        showLastPinned();
//                    }

                    if (position < 1 & presenter.isNewestMessagesLoaded()) {
                        if (!isArrowShowed) return;
                        isArrowShowed = false;
                        performScaleAnimation(1, 0, arrow);
                        return;
                    }

                    if (dy > ARROW_APPEARANCE) {
                        if (isArrowShowed) return;
                        performScaleAnimation(0, 1, arrow);
                        isArrowShowed = true;
                    } else if (dy < ARROW_DISAPPEARANCE) {
                        if (isScrollIndicatorShown) return;
                        if (!isArrowShowed) return;
                        // arrow disappears

                        performScaleAnimation(1, 0, arrow);
                        isArrowShowed = false;
                    }
                }
            });
        }
    }

    @Override
    public void updateScrollArrow() {
        LinearLayout arrow = binding.scrollArrow;

        LinearLayoutManager layoutManager = (LinearLayoutManager)
                binding.recyclerView.getLayoutManager();
        if (layoutManager == null) return;
        int position = layoutManager.findFirstVisibleItemPosition();

        Logger.logDebug("Scroll arrow", " pos - " + position);

        if (position < 1) {
            if (presenter.isNewestMessagesLoaded() && isArrowShowed) {
                isArrowShowed = false;

                performScaleAnimation(1, 0, arrow);
            }
            return;
        }

        if (isArrowShowed) return;
        isArrowShowed = true;
        performScaleAnimation(0, 1, arrow);
    }

    @Override
    public void updateScrollArrowIndicator() {
        if (getRoom().getUnreadMessagesIds().size() == 0 && isScrollIndicatorShown) {
            performScaleAnimation(1, 0, binding.scrollArrowUnreadIndicator);
            isScrollIndicatorShown = false;
        } else if (getRoom().getUnreadMessagesIds().size() != 0 && !isScrollIndicatorShown) {
            performScaleAnimation(0, 1, binding.scrollArrowUnreadIndicator);
            isScrollIndicatorShown = true;
        }
    }

    @Override
    public void showLastPinned(boolean useHideAnimation) {
        currentPinnedShownPos = presenter.getPinnedMessages().size() - 1;
        updatePinned(currentPinnedShownPos, useHideAnimation);
    }

    @Override
    public void showNextPinned() {
        currentPinnedShownPos--;

        int size = presenter.getPinnedMessages().size();
        if (currentPinnedShownPos == -1) currentPinnedShownPos += size;

        updatePinned(currentPinnedShownPos, false);
    }

    @Override
    public void updatePinned(int messagePos, boolean useHideAnimation) {
        runOnUiThread(() -> {
            ArrayList<Message> pinnedMessages = presenter.getPinnedMessages();

            int size = presenter.getPinnedMessages().size();
            Logger.logDebug(RoomActivity.class.getName(), "Pin updated (" +
                    "pos = " + currentPinnedShownPos + ", size = " +
                    size + ")");

            if (size == 0) {
                isLastPinnedShown = false;

                binding.pinnedCount.setVisibility(View.GONE);

                if (useHideAnimation) fillPinnedComponent(null);
                return;
            }

            isLastPinnedShown = currentPinnedShownPos != size - 1;

            binding.pinnedCount.setVisibility(View.VISIBLE);

            String sizeText;
            if (size > 99) sizeText = "99+";
            else sizeText = String.valueOf(size);

            binding.pinnedCount.setText(sizeText);
            Message currentPinned = pinnedMessages.get(currentPinnedShownPos);

            fillPinnedComponent(currentPinned);
        });
    }

    @Override
    public void addMessageToBlinkId(String messageId) {
        messagesToBlinkIds.add(messageId);
    }

    private void fillPinnedComponent(Message message) {
        runOnUiThread(() -> {
            if (message == null) {
                binding.pinnedLayout.setOnClickListener((View v) -> {
                });
            } else {
                currentPinnedTapCount = 0;
                Logger.logDebug(RoomActivity.class.getName(), "New onClick (tapCount = " + currentPinnedTapCount + ")");

                binding.pinnedLayout.setOnClickListener((View v) -> {
                    currentPinnedTapCount++;
                    Logger.logDebug(RoomActivity.class.getName(), "click! (tapCount = " + currentPinnedTapCount + ")");
                    presenter.scrollToMessage(message.getId(), message.getTime());

                    if (currentPinnedTapCount == 2) {
                        binding.pinnedLayout.setOnClickListener((View v2) -> {
                        });
                        Logger.logDebug(RoomActivity.class.getName(), "showNext (tapCount = " + currentPinnedTapCount + ")");
                        showNextPinned();
                    }
                });
            }

            int px = Math.max(2 * DeviceUtils.dpToPx(8, this) + 2 * DeviceUtils.spToPx(12, this),
                    DeviceUtils.dpToPx(8 + 8 + 40, this));
            Logger.logDebug("sdfdsfdsfsdf", "dddddd - " + px);
            fillViews(message, binding.pinnedAuthorName, binding.pinnedText, binding.pinnedImageCard,
                    binding.pinnedImage, px, binding.pinnedLayout);

        });

    }

    @Override
    public void onMediaMessageRecorded(String path, AttachmentType attachmentType) {

        ArrayList<Attachment> attachments = new ArrayList<>();
        final String replyId = presenter.getAndClearReplyId();
        RoomActivityPresenter.AttachmentReadyListener attachmentReadyListener = attachment -> {
            attachments.add(attachment);
            presenter.sendMessage(attachments, "", replyId);
        };
        presenter.uploadAttachment(attachmentType, attachmentReadyListener, path, this);
    }

    @Override
    public void onMediaMessageRecordingStart(AttachmentType attachmentType) {
        if (attachmentType == AttachmentType.VOICE) {
            presenter.sendStatus(UserStatusType.RECORDING_VOICE);
            return;
        }
        presenter.sendStatus(UserStatusType.RECORDING_BUBBLE);
    }

    public void showKeyboard(final EditText ettext) {
        ettext.requestFocus();
        ettext.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                   keyboard.showSoftInput(ettext, 0);
                               }
                           }
                , 100);
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean isKeyboardVisible() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        return imm.isAcceptingText();
    }

    @Override
    public void fillRoomInfo(Bitmap picture, Room room) {
        runOnUiThread(() -> {
            if (picture != null) {
                binding.roomPicture.setImageBitmap(picture);
            }

            TextView roomName = findViewById(R.id.room_name);
            roomName.setText(room.getName());
            if (messagesAdapter != null) return;
            messagesAdapter = new MessagesAdapter(this, new ArrayList<>(), room,
                    new AsyncLayoutInflater(RoomActivity.this), new RoomViewHolderFactory(),
                    getCacheUtils());

            binding.messageTextInput.setText(room.getUnsentText());

            binding.recyclerView.setAdapter(messagesAdapter);

            setOnScrollListener();
            presenter.loadMessagesAtRoomStart();
        });
    }

    @Override
    public void notifyRecyclerMessage(Message message, boolean needUpdateList) {
        if (message.hasAuthor()) {
            if (!message.getAuthorId().equals(getCacheUtils().getString(CacheUtils.ID))) {
                presenter.getRoom().addNewUnreadMessageId(message.getId());
            }
        }
        binding.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (needUpdateList) {
                    messagesAdapter.notifyItemInserted(0);
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
    public void notifyRecyclerMessageRead(Message message, int pos) {
        Logger.logDebug("ReadingDebug", "6");
        if (message.getMessageReadingList().size() > 1) return;
        Logger.logDebug("ReadingDebug", "7");

        runOnUiThread(() -> {
            Logger.logDebug("ReadingDebug", "8 " + pos);

            notifyViewHolder(pos, (BaseMessageViewHolder holder) -> {
                Logger.logDebug("ReadingDebug", "9");
                holder.updateMessageReading(message, true);
            });
        });
    }

    @Override
    public void notifyAdapterItemsChanged(int from, int to) {
        runOnUiThread(() -> {
            messagesAdapter.notifyItemRangeChanged(from, to - from);
        });
    }

    @Override
    public void notifyOnRoomOpenMessagesLoaded(int scrollPosition) {
        notifyMessagesInserted(IS_ROOM_OPENING, 0, scrollPosition);
    }

    @Override
    public void notifyMessageInsertedWithoutScroll(int start, int last) {
        notifyMessagesInserted(start, last, DO_NOT_NEED_TO_SCROLL);
    }

    @Override
    public void notifyMessagesInserted(int start, int last, int scrollPosition) {
        Logger.logDebug("notifying added", "adding item ");
        if (start == IS_ROOM_OPENING) {
            messagesAdapter.notifyDataSetChanged();
        } else {
            messagesAdapter.notifyItemRangeInserted(start, last - start);
        }

        if (scrollPosition != DO_NOT_NEED_TO_SCROLL) {
            binding.recyclerView.scrollToPosition(scrollPosition);
        }
        Logger.logDebug("notifying added", "item has been added successfully ||| " +
                presenter.getItemsCount() + " adapter - " + messagesAdapter.getItemCount());

    }

    @Override
    public void notifyAdapterItemChanged(int index) {
        Logger.logDebug("notifying changed", "item changing");
        messagesAdapter.notifyItemChanged(index);

        Logger.logDebug("notifying changed", "item has been changed successfully" + " ||| " +
                presenter.getItemsCount() + " adapter - " + messagesAdapter.getItemCount());
    }

    @Override
    public void notifyAdapterItemsDeleted(int start, int last) {
        Logger.logDebug("notifying adapter", "Deleted items " + presenter.getItemsCount());
        // binding.recyclerView.getRecycledViewPool().clear();
        messagesAdapter.notifyItemRangeRemoved(start, last - start);
        Logger.logDebug("notifying adapter",
                "Deleted items from start - " + start + " to " + last +
                        " size is " + messagesAdapter.getItemCount() + " ||| " +
                        presenter.getItemsCount() + " adapter - " + messagesAdapter.getItemCount());
    }

    @Override
    public void notifyAdapterItemRemoved(int pos) {
        messagesAdapter.notifyItemRemoved(pos);
    }

    @Override
    public void notifyViewHolderUpdateTimeAndPicture(int pos, Message message, Message previousMessage) {
        notifyViewHolder(pos, (BaseMessageViewHolder holder) -> {
            holder.bindUserPicture(message, previousMessage);
            holder.fillDateAndTime(message, previousMessage);
        });
    }

    @Override
    public void notifyViewHolder(int pos, BaseMessageViewHolder.ViewHolderNotification notification) {
        BaseMessageViewHolder holder = (BaseMessageViewHolder) binding.recyclerView.
                findViewHolderForAdapterPosition(pos);

        boolean notFound = holder == null;

        if (!notFound) notFound = !holder.isInitialized();

        if (notFound) {
            messagesAdapter.notifyItemChanged(pos);
            Logger.logDebug(RoomActivity.class.toString(), "Update message time by notifying");
            return;
        }

        notification.notifyViewHolder(holder);
    }

    @Override
    public void blinkViewHolder(int position) {
        BaseMessageViewHolder holder = (BaseMessageViewHolder) binding.recyclerView.
                findViewHolderForAdapterPosition(position);

        Logger.logDebug("blink", String.valueOf(holder == null));
        if (holder == null) return;

        holder.blink();
    }

    @Override
    public boolean isMessageVisible(int position) {
        LinearLayoutManager manager = (LinearLayoutManager) binding.
                recyclerView.getLayoutManager();
        if (manager == null) return false;

        int first = manager.findFirstVisibleItemPosition() - 2;
        int last = manager.findLastVisibleItemPosition() + 2;

        return position >= first && position <= last;
    }

    @Override
    public void setMessages(List<Message> messages) {
        if (messagesAdapter == null) return;
        messagesAdapter.setMessages(messages);
    }

    @Override
    public DiraRoomDatabase getRoomDatabase() {
        return DiraRoomDatabase.getDatabase(getApplicationContext());
    }

    @Override
    public DiraMessageDatabase getMessagesDatabase() {
        return DiraMessageDatabase.getDatabase(getApplicationContext());
    }

    @Override
    public Room getRoom() {
        return presenter.getRoom();
    }

    @Override
    public HashMap<String, Member> getMembers() {
        return presenter.getMembers();
    }

    @Override
    public Context getContext() {
        return RoomActivity.this;
    }

    @Override
    public MessageReplyListener getReplyListener() {
        return (MessageReplyListener) presenter;
    }

    @Override
    public boolean isMediaPreviewActivityOpened() {
        return isMediaPreviewActivityOpened;
    }

    @Override
    public PreparedActivity preparePreviewActivity(String filePath, boolean isVideo,
                                                   Bitmap preview, View transitionSource,
                                                   Attachment attachment) {
        isMediaPreviewActivityOpened = true;

        Intent intent = new Intent(this, MediaPreviewActivity.class);
        intent.putExtra(MediaPreviewActivity.ROOM_SECRET, roomSecret);
        intent.putExtra(MediaPreviewActivity.START_ATTACHMENT_ID, attachment.getId());
        intent.putExtra(MediaPreviewActivity.START_ATTACHMENT_URL, attachment.getFileUrl());

//        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
//                this,
//                Pair.create(transitionSource, getString(R.string.transition_image_shared)));

        ActivityOptions options = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            options = ActivityOptions.makeBasic();
        } else {
            options = ActivityOptions.makeTaskLaunchBehind();
        }

        return new PreparedActivity.MessagePreviewPreparedActivity(this, intent, options);
    }

    @Override
    public void attachVideoPlayer(DiraVideoPlayer player) {
        player.attachDiraActivity(this);
        player.attachRecyclerView(binding.recyclerView);
    }

    @Override
    public BalloonMessageMenu.BalloonMenuListener getBalloonMessageListener() {
        return this;
    }

    @Override
    public void onMessageAttached(Message message) {
//        if (getRoom().getPinnedMessagesIds().contains(message.getId())) {
//            updatePinned();
//        }
    }

    @Override
    public void onMessageDetached(Message message) {
//        if (getRoom().getPinnedMessagesIds().contains(message.getId())) {
//            updatePinned();
//        }
    }

    @Override
    public boolean isMessageNeedBlink(String messageId) {
        if (messagesToBlinkIds.contains(messageId)) {
            messagesToBlinkIds.remove(messageId);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCurrentListeningAppeared(ListenableViewHolder viewHolder) {
        if (GlobalMediaPlayer.getInstance().getCurrentMessage() == null) return false;

        if (viewHolder.getCurrentMessage().getId().equals(
                GlobalMediaPlayer.getInstance().getCurrentMessage().getId())) {
            currentListenableViewHolder = viewHolder;
            GlobalMediaPlayer player = GlobalMediaPlayer.getInstance();

            currentListenableViewHolder.rebindPlaying(player.isPaused(), player.getCurrentProgress());
            Logger.logDebug("RoomActivity", "Current listenable has been found");
            return true;
        }
        return false;
    }

    @Override
    public boolean isCurrentListeningDisappeared(ListenableViewHolder viewHolder) {
        if (GlobalMediaPlayer.getInstance().getCurrentMessage() == null) return false;

        if (viewHolder.getCurrentMessage().getId().equals(
                GlobalMediaPlayer.getInstance().getCurrentMessage().getId())) {

            currentListenableViewHolder = null;
            Logger.logDebug("RoomActivity", "Current listenable has been recycled");
            return true;
        }
        return false;
    }

    @Override
    public void currentListenableStarted(ListenableViewHolder viewHolder, File file, float progress) {
        if (currentListenableViewHolder != null) {
            currentListenableViewHolder.clearProgress();
        }

        currentListenableViewHolder = viewHolder;

        GlobalMediaPlayer.getInstance().changePlyingMessage(
                viewHolder.getCurrentMessage(), file, progress);
    }

    @Override
    public void currentListenablePaused(ListenableViewHolder viewHolder) {
        currentListenableViewHolder = viewHolder;
        GlobalMediaPlayer.getInstance().onPaused();
    }

    @Override
    public void currentListenableProgressChangedByUser(float progress, ListenableViewHolder viewHolder) {

        if (GlobalMediaPlayer.getInstance().setCurrentProgress(progress)) {
            currentListenableViewHolder = viewHolder;
        }
    }

    @Override
    public boolean isSecurePrivateRoom() {
        return getRoom().getRoomType() == RoomType.PRIVATE && getMembers().size() == 1;
    }

    @Override
    public void onFirstMessageScrolled(Message message, int index) {
        presenter.loadMessagesBefore(message, index);
    }

    @Override
    public void onLastLoadedMessageDisplayed(Message message, int index) {
        presenter.loadNewerMessage(message, index);
    }

    @Override
    public void onSelectedFilesSent(List<SelectorFileInfo> diraMediaInfoList, String messageText) {
        MultiAttachmentLoader multiAttachmentLoader = new MultiAttachmentLoader(messageText, presenter);
        multiAttachmentLoader.send(diraMediaInfoList, this);
    }

    @Override
    public void onBalloonShown() {

        if (isKeyboardVisible()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }

    }

    @Override
    public void onNewMessageReply(Message message) {
        presenter.setReplyingMessage(message);
        setReplyMessage(message);
    }

    @Override
    public void onMessageDelete(Message message) {
        presenter.deleteMessage(message, this);
    }

    @Override
    public boolean isMessagePinned(String messageId) {
        return presenter.isPinned(messageId);
    }

    @Override
    public void sendRequest(Request request) {
        try {
            UpdateProcessor.getInstance().sendRequest(request, getRoom().getServerAddress());
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGlobalMediaPlayerPauseClicked(boolean isPaused, float progress) {
        if (currentListenableViewHolder == null) return;

        currentListenableViewHolder.pause(isPaused, progress);
    }

    @Override
    public void onGlobalMediaPlayerClose() {
        if (currentListenableViewHolder == null) return;

        currentListenableViewHolder.clearProgress();

        currentListenableViewHolder = null;
    }

    @Override
    public void onGlobalMediaPlayerStart(Message message, File file) {
        if (currentListenableViewHolder == null) {
            // find new ViewHolder somehow. Remove return
            return;
        }

        currentListenableViewHolder.start();
    }

    @Override
    public void onGlobalMediaPlayerProgressChanged(float progress, Message message) {
        if (currentListenableViewHolder == null) {
            Logger.logDebug("RoomActivity", "currentListenableViewHolder = null!");
            return;
        }

        if (!message.getId().equals(currentListenableViewHolder.getCurrentMessage().getId())) {
            throw new RuntimeException("Wrong Listenable ViewHolder playing listenable message");
        }
        currentListenableViewHolder.setProgress(progress);
    }
}