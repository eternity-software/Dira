package com.diraapp.ui.adapters.messages;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.MessageReadRequest;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.adapters.messagetooltipread.MessageTooltipAdapter;
import com.diraapp.ui.adapters.messagetooltipread.UserReadMessage;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MessageAttachmentToLargeView;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;
import com.diraapp.ui.components.RoomMessageVideoPlayer;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.ui.components.VoiceMessageView;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;
import com.masoudss.lib.SeekBarOnProgressChanged;
import com.masoudss.lib.WaveformSeekBar;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonOutsideTouchListener;
import com.skydoves.balloon.radius.RadiusLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class RoomMessagesAdapter extends RecyclerView.Adapter<ViewHolder> {


    public static final int VIEW_TYPE_CLIENT_DATA = 50;
    public static final int VIEW_TYPE_SELF_MESSAGE = 0;
    public static final int VIEW_TYPE_SELF_MESSAGE_BUBBLE = 1;
    public static final int VIEW_TYPE_SELF_MESSAGE_VOICE = 2;
    public static final int VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS = 3;
    public static final int VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS_TOO_LARGE = 4;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 20;
    public static final int VIEW_TYPE_ROOM_MESSAGE_BUBBLE = 21;
    public static final int VIEW_TYPE_ROOM_MESSAGE_VOICE = 22;
    public static final int VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS = 23;
    public static final int VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS_TOO_LARGE = 24;
    private static Thread thread;
    private final LayoutInflater layoutInflater;

    private final String selfId;
    private final DiraActivity context;
    private final List<AttachmentsStorageListener> listeners = new ArrayList<>();
    private final CacheUtils cacheUtils;
    private final HashMap<String, Bitmap> loadedBitmaps = new HashMap<>();
    private final String secretName;
    private final String serverAddress;
    private final RecyclerView recyclerView;
    private final long maxAutoLoadSize;
    private final DiraMediaPlayer diraMediaPlayer = new DiraMediaPlayer();
    private final MessageAdapterListener messageAdapterListener;
    private Room room;
    private ColorTheme theme;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();
    private String firstLoadedId;


    public RoomMessagesAdapter(DiraActivity context, RecyclerView recyclerView, String secretName, String serverAddress, Room room, MessageAdapterListener messageAdapterListener) {
        this.context = context;
        this.secretName = secretName;
        this.serverAddress = serverAddress;
        this.messageAdapterListener = messageAdapterListener;
        this.room = room;
        this.recyclerView = recyclerView;
        layoutInflater = LayoutInflater.from(context);
        cacheUtils = new CacheUtils(context);


        selfId = cacheUtils.getString(CacheUtils.ID);
        maxAutoLoadSize = cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE);
        theme = AppTheme.getInstance().getColorTheme();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder;
        if (viewType >= VIEW_TYPE_ROOM_MESSAGE) {
            holder = new ViewHolder(layoutInflater.inflate(R.layout.room_message, parent, false));
        } else {
            holder = new ViewHolder(layoutInflater.inflate(R.layout.self_message, parent, false));
        }

        return displayMessageView(holder, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        boolean isSelfMessage = false;
        if (message.getAuthorId() != null) {
            isSelfMessage = selfId.equals(message.getAuthorId());
        }

        return defineMessageType(message, isSelfMessage);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.videoPlayer != null) holder.videoPlayer.release();
        if (holder.bubblePlayer != null) holder.bubblePlayer.release();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.videoPlayer != null) holder.videoPlayer.release();
        if (holder.bubblePlayer != null) holder.bubblePlayer.release();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (holder.attachmentsStorageListener != null) {
            AttachmentsStorage.removeAttachmentsStorageListener(holder.attachmentsStorageListener);
            listeners.remove(holder.attachmentsStorageListener);
        }

        holder.loading.setVisibility(View.GONE);

        if (holder.videoPlayer != null) {
            if (holder.videoPlayer.getVisibility() == View.VISIBLE) {
                holder.videoPlayer.release();
            }
        }
        /*
        holder.messageText.setVisibility(View.VISIBLE);
        holder.videoPlayer.release();
        holder.videoPlayer.setRecyclerView(recyclerView);
        holder.bubblePlayer.setRecyclerView(recyclerView);
        holder.bubblePlayer.release();
        holder.videoPlayer.setDelay(10);
        holder.bubblePlayer.setDelay(10);
        holder.voiceLayout.setVisibility(View.GONE);
        holder.loading.setVisibility(View.GONE);
        holder.sizeContainer.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);
        holder.videoPlayer.setVisibility(View.GONE);
        holder.dateText.setVisibility(View.GONE);
        holder.bubbleContainer.setVisibility(View.GONE); */

        Message message = messages.get(position);

        if (position == messages.size() - 1) {
            messageAdapterListener.onFirstItemScrolled(message, position);
        }
        if (!message.isRead() && message.getCustomClientData() == null) {
            if (message.getAuthorId() == null) return;
            if (!message.getAuthorId().equals(selfId)) {
                message.setRead(true);

                MessageReadRequest request = new MessageReadRequest(selfId, System.currentTimeMillis(),
                        message.getId(), message.getRoomSecret());
                try {
                    UpdateProcessor.getInstance().sendRequest(request, serverAddress);
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                }
            }
        }

        Message previousMessage = null;

        boolean isSameDay = false;
        boolean isSameYear = false;

        if (position < messages.size() - 1) {
            previousMessage = messages.get(position + 1);
            Date date = new Date(message.getTime());
            Date datePrev = new Date(previousMessage.getTime());

            Calendar calendar = Calendar.getInstance();
            Calendar calendarPrev = Calendar.getInstance();

            calendar.setTime(date);
            calendarPrev.setTime(datePrev);

            if (calendar.get(Calendar.DAY_OF_YEAR) == calendarPrev.get(Calendar.DAY_OF_YEAR)) {
                isSameDay = true;
            }
            if (calendar.get(Calendar.YEAR) == calendarPrev.get(Calendar.YEAR)) {
                isSameYear = true;
            }
        }

        if (!isSameDay || !isSameYear) {

            String dateString = Numbers.getDateFromTimestamp(message.getTime(), !isSameYear);
            holder.dateText.setVisibility(View.VISIBLE);
            holder.dateText.setText(dateString);
        } else {
            holder.dateText.setVisibility(View.GONE);
        }

        if (message.getCustomClientData() == null) {
            bindUserMessage(message, previousMessage, isSameDay, isSameYear, holder);
        } else {
            bindRoomUpdateMessage(message, holder);
        }

        holder.timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime(), context));
    }

    public void updateAttachment(ViewHolder holder, Attachment attachment, File file) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                    holder.imageView.setVisibility(View.VISIBLE);

                    Picasso.get().load(Uri.fromFile(file)).into(holder.imageView);
                    holder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            PreviewActivity.open(context, file.getPath(),
                                    attachment.getAttachmentType() == AttachmentType.VIDEO, holder.imageContainer);
                        }
                    });
                    holder.loading.setVisibility(View.GONE);
                } else if (attachment.getAttachmentType() == AttachmentType.VIDEO || attachment.getAttachmentType() == AttachmentType.BUBBLE) {


                    VideoPlayer videoPlayer = holder.videoPlayer;

                    if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                        videoPlayer = holder.bubblePlayer;
                        videoPlayer.setLoadingLayerEnabled(true);
                        holder.bubbleContainer.setVisibility(View.VISIBLE);

                    } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                        holder.imageView.setVisibility(View.VISIBLE);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                        int width = 1;
                        int height = 1;
                        int rotation = 0;
                        try {
                            retriever.setDataSource(file.getPath());
                            width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                            rotation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

                            if (rotation != 0) {
                                width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                                height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, false,
                                    attachment, secretName);
                            AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, serverAddress);
                            file.delete();
                            return;
                        }

                        if (height > width * 3) {
                            height = width * 2;
                        }

                        try {
                            retriever.release();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                        Bitmap bmp = Bitmap.createBitmap(width, height, conf);
                        holder.imageView.setImageBitmap(bmp);

                        videoPlayer.setVisibility(View.VISIBLE);
                        VideoPlayer finalVideoPlayer = videoPlayer;
                        holder.imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                finalVideoPlayer.getLayoutParams().height = holder.imageView.getMeasuredHeight();
                                finalVideoPlayer.getLayoutParams().width = holder.imageView.getMeasuredWidth();
                                finalVideoPlayer.requestLayout();

                                finalVideoPlayer.setVolume(0);

                                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                                alphaAnimation.setDuration(500);

                                alphaAnimation.setFillAfter(true);
                                finalVideoPlayer.startAnimation(alphaAnimation);
                            }
                        });

                    }
                    try {
                        videoPlayer.play(file.getPath());

                        holder.loading.setVisibility(View.GONE);
                        // holder.messageContainer.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    VideoPlayer finalVideoPlayer = videoPlayer;
                    videoPlayer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (attachment.getAttachmentType() == AttachmentType.VIDEO) {


                                PreviewActivity.open(context, file.getPath(),
                                        attachment.getAttachmentType() == AttachmentType.VIDEO, holder.imageContainer);
                            } else {

                                try {
                                    if (diraMediaPlayer.isPlaying()) {
                                        diraMediaPlayer.stop();
                                    }
                                    diraMediaPlayer.reset();
                                    diraMediaPlayer.setDataSource(file.getPath());


                                    diraMediaPlayer.prepareAsync();


                                    diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            diraMediaPlayer.start();
                                            finalVideoPlayer.setVolume(0);
                                            finalVideoPlayer.setProgress(0);
                                            finalVideoPlayer.setSpeed(1f);
                                            diraMediaPlayer.setOnPreparedListener(null);

                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    VideoPlayer finalVideoPlayer2 = videoPlayer;
                    videoPlayer.setVideoPlayerListener(new VideoPlayer.VideoPlayerListener() {
                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onPaused() {

                        }

                        @Override
                        public void onReleased() {

                        }

                        @Override
                        public void onReady(int width, int height) {
                            try {
                                finalVideoPlayer2.play(file.getPath());
                                finalVideoPlayer2.setVideoPlayerListener(null);
                                finalVideoPlayer2.setVolume(0);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            holder.loading.setVisibility(View.GONE);
                        }
                    });
                }  else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                    holder.loading.setVisibility(View.GONE);
                    holder.waveformSeekBar.setSampleFrom(file);
                    holder.waveformSeekBar.setProgress(attachment.getVoiceMessageStopPoint());
                    holder.voiceLayout.setVisibility(View.VISIBLE);

                    holder.playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            try {
                                if (diraMediaPlayer.isPlaying()) {
                                    diraMediaPlayer.stop();
                                }
                                diraMediaPlayer.reset();
                                diraMediaPlayer.setDataSource(file.getPath());

                                diraMediaPlayer.prepareAsync();

                                diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        diraMediaPlayer.start();
                                        holder.waveformSeekBar.setOnProgressChanged(new SeekBarOnProgressChanged() {
                                            @Override
                                            public void onProgressChanged(@NonNull WaveformSeekBar waveformSeekBar, float v, boolean fromUser) {
                                                if (fromUser) {
                                                    diraMediaPlayer.setProgress(v / 10);
                                                }
                                            }
                                        });

                                        diraMediaPlayer.setOnProgressTick(new Runnable() {
                                            @Override
                                            public void run() {
                                                context.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            float progress = 10 * diraMediaPlayer.getProgress();
                                                            holder.waveformSeekBar.setProgress(progress);
                                                            attachment.setVoiceMessageStopProgress(progress);
                                                        } catch (Exception ignored) {
                                                        }
                                                    }
                                                });
                                            }
                                        });


                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });
    }

    public void release() {
        for (AttachmentsStorageListener attachmentsStorageListener : listeners) {
            AttachmentsStorage.removeAttachmentsStorageListener(attachmentsStorageListener);
        }
        diraMediaPlayer.reset();
        diraMediaPlayer.release();

    }

    public HashMap<String, Member> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Member> members) {
        this.members = members;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void bindUserMessage(Message message, Message previousMessage,
                                 boolean isSameDay, boolean isSameYear, ViewHolder holder) {
        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener((View v) -> {
            createBalloon(message, holder.itemView);
        });

        boolean isSelfMessage = selfId.equals(
                message.getAuthorId());

        if (message.getText() == null) {
            holder.messageText.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.GONE);
        } else if (message.getText().length() == 0) {
            holder.messageText.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.GONE);
        } else if (StringFormatter.isEmoji(message.getText()) && StringFormatter.getEmojiCount(message.getText()) < 3) {
            holder.messageContainer.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.VISIBLE);
            holder.emojiText.setText(message.getText());
        } else {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.emojiText.setVisibility(View.GONE);
            holder.messageText.setText(message.getText());
        }

        loadMessageAttachment(message, holder);
        if (!isSelfMessage) {
            if (members.containsKey(message.getAuthorId())) {

                Member member = members.get(message.getAuthorId());
                holder.nicknameText.setText(member.getNickname());

                boolean showProfilePicture = true;
                if (previousMessage != null) {
                    if (previousMessage.getAuthorId() != null) {
                        if (previousMessage.getAuthorId().equals(message.getAuthorId()) && isSameDay
                                && isSameYear) {
                            holder.pictureContainer.setVisibility(View.INVISIBLE);
                            holder.nicknameText.setVisibility(View.GONE);
                            showProfilePicture = false;
                        }
                    }
                }

                if (showProfilePicture) {
                    if (member.getImagePath() != null) {
                        Picasso.get().load(new File(member.getImagePath())).into(holder.profilePicture);
                    } else {
                        holder.profilePicture.setImageResource(R.drawable.placeholder);
                    }
                    holder.pictureContainer.setVisibility(View.VISIBLE);
                    holder.nicknameText.setText(message.getAuthorNickname());
                    holder.nicknameText.setVisibility(View.VISIBLE);
                }

            }
        } else {
            if (message.getMessageReadingList() != null) {
                if (message.getMessageReadingList().size() == 0) {
                    holder.messageBackground.getBackground().setColorFilter(
                            theme.getUnreadMessageBackground(), PorterDuff.Mode.SRC_IN);
                    holder.messageBackground.getBackground().setAlpha((int) (255 * 0.18));
                } else {
                    holder.messageBackground.getBackground().setColorFilter(
                            Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
                }
            }

        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void bindRoomUpdateMessage(Message message, ViewHolder holder) {
        holder.itemView.setClickable(false);

        holder.nicknameText.setVisibility(View.GONE);
        holder.pictureContainer.setVisibility(View.VISIBLE);

        holder.messageText.setVisibility(View.GONE);

        if (room.getImagePath() != null) {
            Picasso.get().load(new File(room.getImagePath())).into(holder.profilePicture);
        }

        if (message.getCustomClientData() instanceof RoomJoinClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_new_member)
                    .replace("%s", ((RoomJoinClientData)
                            message.getCustomClientData()).getNewNickName()));

            String path = ((RoomJoinClientData) message.getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(holder, path);
            holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_room_updates));
            holder.roomUpdatesText.setVisibility(View.GONE);
        } else if (message.getCustomClientData() instanceof RoomNameChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_name_change));

//            holder.roomUpdatesText.setText(context.getString(R.string.room_update_name)
//                    .replace("%s", ((RoomNameChangeClientData)
//                            message.getCustomClientData()).getOldName())
//                    .replace("%p", ((RoomNameChangeClientData)
//                            message.getCustomClientData()).getNewName()));
            holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_room_updates));
            holder.roomUpdatesText.setText(((RoomNameChangeClientData) message.getCustomClientData()).getOldName());

            applyDefaultIconOnUpdateMessage(holder);

            holder.roomUpdatesText.setVisibility(View.VISIBLE);
        } else if (message.getCustomClientData() instanceof RoomIconChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_picture_change));

            String path = ((RoomIconChangeClientData) message.getCustomClientData()).getImagePath();
            setImageOnRoomUpdateMessage(holder, path);

            holder.roomUpdatesText.setVisibility(View.GONE);
        } else if (message.getCustomClientData() instanceof RoomNameAndIconChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_name_and_picture_change));

//            holder.roomUpdatesText.setText(context.getString(R.string.room_update_name)
//                    .replace("%s", ((RoomNameAndIconChangeClientData)
//                            message.getCustomClientData()).getOldName())
//                    .replace("%p", ((RoomNameAndIconChangeClientData)
//                            message.getCustomClientData()).getNewName()));

            holder.roomUpdatesText.setText(((RoomNameAndIconChangeClientData) message.getCustomClientData()).getOldName());

            String path = ((RoomNameAndIconChangeClientData) message.getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(holder, path);

            holder.roomUpdatesText.setVisibility(View.VISIBLE);
        } else if (message.getCustomClientData() instanceof KeyGenerateStartClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.key_generate_start));
            holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_encryption));
            applyDefaultIconOnUpdateMessage(holder);
            holder.roomUpdatesText.setVisibility(View.GONE);
        } else if (message.getCustomClientData() instanceof KeyGeneratedClientData) {
            holder.roomUpdatesMainText.setText(((KeyGeneratedClientData) message.
                    getCustomClientData()).getClientDataText(context));

            if (Objects.equals(((KeyGeneratedClientData) message.
                    getCustomClientData()).getResult(), KeyGeneratedClientData.RESULT_CANCELLED)) {
                holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_encryption_disabled));
            } else {
                holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_encryption));
            }
            applyDefaultIconOnUpdateMessage(holder);
            holder.roomUpdatesText.setVisibility(View.GONE);
        }
    }

    private void applyRoomUpdateMessagesColorTheme(ViewHolder holder) {
        holder.messageContainer.getBackground().setColorFilter(theme.getMessageColor(), PorterDuff.Mode.SRC_IN);
    }

    private void setImageOnRoomUpdateMessage(ViewHolder holder, String path) {

        if (path == null) {
            holder.roomUpdatesIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.placeholder));
            holder.roomUpdatesIcon.setImageTintList(null);
            holder.roomUpdatesIcon.getBackground().setColorFilter(context.
                    getResources().getColor(R.color.dark), PorterDuff.Mode.SRC_IN);
        } else {
            Picasso.get().load(new File(path)).into(holder.roomUpdatesIcon);
            holder.roomUpdatesIcon.setImageTintList(null);
        }
    }

    private void applyDefaultIconOnUpdateMessage(ViewHolder holder) {
        holder.roomUpdatesIcon.setImageTintList(ColorStateList.valueOf(
                AppTheme.getInstance().getColorTheme().getTextColor()));
        holder.roomUpdatesIcon.getBackground().setColorFilter(AppTheme.getInstance().getColorTheme().getMessageColor(), PorterDuff.Mode.SRC_IN);
    }

    private void applyUserMessageColorTheme(ViewHolder holder, boolean isSelfMessage) {
        if (isSelfMessage) {
            holder.messageText.setTextColor(theme.getSelfTextColor());
            holder.messageText.setLinkTextColor(theme.getSelfLinkColor());
            holder.messageContainer.getBackground().setColorFilter(theme.getSelfMessageColor(), PorterDuff.Mode.SRC_IN);
        } else {
            holder.messageText.setTextColor(theme.getTextColor());
            holder.messageText.setLinkTextColor(theme.getRoomLickColor());
            holder.messageContainer.getBackground().setColorFilter(theme.getMessageColor(), PorterDuff.Mode.SRC_IN);
        }

        holder.attachmentProgressbar.setIndeterminateTintList(ColorStateList.
                valueOf(theme.getSelfTextColor()));
    }

    private void loadMessageAttachment(Message message, ViewHolder holder) {
        String encryptionKey = "";
        if (room != null) {
            if (message.getLastTimeEncryptionKeyUpdated() == room.getTimeEncryptionKeyUpdated()) {
                encryptionKey = room.getEncryptionKey();
            }
        }


        if (message.getAttachments() != null) {
            if (message.getAttachments().size() > 0) {
                holder.attachmentsStorageListener = new AttachmentsStorageListener() {
                    @Override
                    public void onAttachmentBeginDownloading(Attachment attachment) {

                    }

                    @Override
                    public void onAttachmentDownloaded(Attachment attachment) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl())) {

                                    holder.loading.setVisibility(View.GONE);
                                    File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                                    if (file != null) {
                                        updateAttachment(holder, attachment, file);
                                    } else {
                                        holder.loading.setVisibility(View.VISIBLE);
                                        SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                                        AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, serverAddress);
                                    }

                                }
                            }
                        });

                    }

                    @Override
                    public void onAttachmentDownloadFailed(Attachment attachment) {
                        if (attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl())) {
                            System.out.println("failed");
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.loading.setVisibility(View.GONE);
                                    holder.imageView.setImageResource(R.drawable.ic_trash);
                                    holder.imageView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                };

                AttachmentsStorage.addAttachmentsStorageListener(holder.attachmentsStorageListener);
                listeners.add(holder.attachmentsStorageListener);

                Attachment attachment = message.getAttachments().get(0);

                holder.loading.setVisibility(View.VISIBLE);
                File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                if (file != null && !AttachmentsStorage.isAttachmentSaving(attachment)) {

                    updateAttachment(holder, attachment, file);
                } else {
                    long attachmentSize = attachment.getSize();
                    if (attachmentSize > maxAutoLoadSize) {
                        holder.loading.setVisibility(View.GONE);
                        holder.sizeText.setText(AppStorage.getStringSize(attachmentSize));
                        String finalEncryptionKey = encryptionKey;
                        holder.buttonDownload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                holder.buttonDownload.setVisibility(View.GONE);
                                //   holder.sizeContainer.setVisibility(View.GONE);
                                holder.loading.setVisibility(View.VISIBLE);

                                // TODO: handle if view changed

                                thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            File savedFile = AttachmentsStorage.saveAttachment(context, attachment, message.getRoomSecret(), false, new DownloadHandler() {
                                                @Override
                                                public void onProgressChanged(int progress) {
                                                    context.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            holder.sizeText.setText(AppStorage.getStringSize(attachmentSize) + " (" + progress + "%)");
                                                        }
                                                    });

                                                }
                                            }, serverAddress, finalEncryptionKey);
                                            context.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        int type = defineMessageType(message, selfId.equals(message.getAuthorId()));
                                                        displayMessageView(holder, type);

                                                        if (type == VIEW_TYPE_SELF_MESSAGE_BUBBLE || type == VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
                                                            holder.bubbleViewContainer.requestLayout();
                                                        } else {
                                                            holder.viewsContainer.requestLayout();
                                                        }

                                                        updateAttachment(holder, attachment, savedFile);

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            }
                        });
                    } else {
                        if (!AttachmentsStorage.isAttachmentSaving(attachment)) {
                            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                            AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, serverAddress);
                        }
                    }

                }
            }
        }
    }

    public void setTheme(ColorTheme theme) {
        this.theme = theme;
    }

    private void createBalloon(Message message, View view) {
        ArrayList<UserReadMessage> userReadMessages = new ArrayList<>(
                message.getMessageReadingList().size());

        for (MessageReading messageReading : message.getMessageReadingList()) {
            Member member = members.get(messageReading.getUserId());
            if (member == null) continue;
            UserReadMessage userReadMessage = new UserReadMessage(
                    member.getNickname(), member.getImagePath());
            userReadMessages.add(userReadMessage);
        }

        Balloon balloon = new Balloon.Builder(context).
                setLayout(R.layout.message_actions_tooltip)
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setIsVisibleArrow(false)
                .build();

        RadiusLayout layout = (RadiusLayout) balloon.getContentView();
        layout.setBackground(ContextCompat.getDrawable(context, R.drawable.tooltip_drawable));
        RelativeLayout recyclerLayout = layout.findViewById(R.id.recycler_layout);
        LinearLayout copyRow = layout.findViewById(R.id.copy_row);
        LinearLayout countRow = layout.findViewById(R.id.count_row);
        RecyclerView recyclerView = layout.findViewById(R.id.message_tooltip_recycler);

        int size = userReadMessages.size();
        CardView firstCard = countRow.findViewById(R.id.card_view_1);
        CardView secondCard = countRow.findViewById(R.id.card_view_2);
        ImageView firstUserIcon = countRow.findViewById(R.id.icon_user_1);
        ImageView secondUserIcon = countRow.findViewById(R.id.icon_user_2);
        TextView countTextView = countRow.findViewById(R.id.count_row_text);
        ImageView backArrow = countRow.findViewById(R.id.count_row_arrow);

        final boolean[] isInitialDisplay = {true};

        backArrow.setVisibility(View.GONE);
        recyclerLayout.setVisibility(View.GONE);

        int height = size * 48;
        if (size > 4) {
            height = 4 * 48;
        }
        height = Numbers.dpToPx(height, context);

        if (size == 0) {
            firstCard.setVisibility(View.GONE);
            secondCard.setVisibility(View.GONE);
            countTextView.setText(context.getString(R.string.message_tooltip_zero_read));
        } else if (size == 1) {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            secondCard.setVisibility(View.GONE);
            countTextView.setText(context.getString(R.string.message_tooltip_one_read).
                    replace("%s", String.valueOf(size)));
        } else {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            if (userReadMessages.get(1).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(1).getPicturePath())).into(secondUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            countTextView.setText(context.getString(R.string.message_tooltip_read_count).
                    replace("%s", String.valueOf(size)));
        }

        MessageTooltipAdapter adapter = new MessageTooltipAdapter(context, userReadMessages);
        recyclerView.setAdapter(adapter);

        copyRow.setOnClickListener((View v) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", message.getText());
            clipboard.setPrimaryClip(clip);
            balloon.dismiss();
        });

        if (size != 0) {
            int finalHeight = height;
            countRow.setOnClickListener((View v) -> {
                if (isInitialDisplay[0]) {
                    backArrow.setVisibility(View.VISIBLE);
                    recyclerLayout.setVisibility(View.VISIBLE);
                    recyclerLayout.getLayoutParams().height = finalHeight;
                    copyRow.setVisibility(View.GONE);
                    firstCard.setVisibility(View.GONE);
                    secondCard.setVisibility(View.GONE);
                } else {
                    firstCard.setVisibility(View.VISIBLE);
                    if (size != 1) {
                        secondCard.setVisibility(View.VISIBLE);
                    }
                    copyRow.setVisibility(View.VISIBLE);
                    backArrow.setVisibility(View.GONE);
                    recyclerLayout.setVisibility(View.GONE);
                }
                isInitialDisplay[0] = !isInitialDisplay[0];
            });
        }

        balloon.setOnBalloonOutsideTouchListener((new OnBalloonOutsideTouchListener() {
            @Override
            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                balloon.dismiss();
            }
        }));

        balloon.showAlignBottom(view);
    }

    public interface MessageAdapterListener {
        void onFirstItemScrolled(Message message, int index);
    }

    private int defineMessageType(Message message, boolean isSelfMessage) {
        int type = VIEW_TYPE_SELF_MESSAGE;
        if (message.getCustomClientData() != null) {
            type = VIEW_TYPE_CLIENT_DATA;
        } else if (message.getAttachments().size() != 0) {

            boolean isAttachmentTooLarge = false;
            if (message.getAttachments().get(0).getSize() > maxAutoLoadSize) {
                if (AttachmentsStorage.getFileFromAttachment(message.getAttachments().get(0),
                        context, message.getRoomSecret()) == null) {
                    type = VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS_TOO_LARGE;
                    isAttachmentTooLarge = true;
                }
            }

            if (!isAttachmentTooLarge) {
                if (message.getAttachments().size() > 1) {
                    type = VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS;
                } else if (message.getAttachments().get(0).getAttachmentType() == AttachmentType.BUBBLE) {
                    type = VIEW_TYPE_SELF_MESSAGE_BUBBLE;
                } else if (message.getAttachments().get(0).getAttachmentType() == AttachmentType.VIDEO ||
                        message.getAttachments().get(0).getAttachmentType() == AttachmentType.IMAGE) {
                    type = VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS;
                } else if (message.getAttachments().get(0).getAttachmentType() == AttachmentType.VOICE) {
                    type = VIEW_TYPE_SELF_MESSAGE_VOICE;
                }
            }
        }

        if (type != VIEW_TYPE_CLIENT_DATA) {
            if (!isSelfMessage) {
                type += VIEW_TYPE_ROOM_MESSAGE;
            }
        }
        
        return type;
    }

    private ViewHolder displayMessageView(ViewHolder holder, int viewType) {
        boolean isSelfMessage = viewType < VIEW_TYPE_ROOM_MESSAGE;

        if (isSelfMessage) viewType += VIEW_TYPE_ROOM_MESSAGE;

        View view = null;

        boolean bubbleAdded = false;

        if (viewType == VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
            CardView bubble  = new BubbleMessageView(context);
            holder.bubbleViewContainer.addView(bubble);
            bubble.setCardBackgroundColor(Color.TRANSPARENT);
            bubble.setCardElevation(0);

            holder.messageContainer.setVisibility(View.GONE);
            holder.updateViews();
            holder.viewsContainer.setVisibility(View.GONE);
            holder.bubblePlayer.setDelay(10);

            bubbleAdded = true;
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_VOICE) {
            view  = new VoiceMessageView(context, theme, isSelfMessage);
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS_TOO_LARGE) {
            view  = new MessageAttachmentToLargeView(context, theme, isSelfMessage);
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS) {
            view  = new RoomMessageVideoPlayer(context);
            ((CardView) view).setCardBackgroundColor(Color.TRANSPARENT);
        }  else if (viewType == VIEW_TYPE_CLIENT_DATA) {
            view  = new RoomMessageCustomClientDataView(context, theme);
        }

        if (!bubbleAdded & view != null) {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.viewsContainer.addView(view);
            holder.updateViews();
            if (holder.videoPlayer != null) holder.videoPlayer.setDelay(10);
        }

        if (viewType != VIEW_TYPE_CLIENT_DATA) {
            applyUserMessageColorTheme(holder, isSelfMessage);
        } else {
            applyRoomUpdateMessagesColorTheme(holder);
        }

        return holder;
    }

}
