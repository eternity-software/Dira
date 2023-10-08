package com.diraapp.ui.adapters.messages.legacy;

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
import android.widget.FrameLayout;
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
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.adapters.messagetooltipread.MessageTooltipAdapter;
import com.diraapp.ui.adapters.messagetooltipread.UserReadMessage;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MessageAttachmentToLargeView;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.ui.components.MultiAttachmentMessageView;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;
import com.diraapp.ui.components.RoomMessageVideoPlayer;
import com.diraapp.ui.components.VoiceMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;
import com.diraapp.utils.Timer;
import com.felipecsl.asymmetricgridview.library.model.AsymmetricItem;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import linc.com.amplituda.Amplituda;

public class LegacyRoomMessagesAdapter extends RecyclerView.Adapter<LegacyViewHolder> {


    public static final int VIEW_TYPE_CLIENT_DATA = 50;
    public static final int VIEW_TYPE_SELF_MESSAGE = 0;
    public static final int VIEW_TYPE_SELF_MESSAGE_BUBBLE = 1;
    public static final int VIEW_TYPE_SELF_MESSAGE_VOICE = 2;
    public static final int VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS = 3;
    public static final int VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS_TOO_LARGE = 4;
    public static final int VIEW_TYPE_SELF_MESSAGE_MULTI_ATTACHMENTS = 5;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 20;
    public static final int VIEW_TYPE_ROOM_MESSAGE_BUBBLE = 21;
    public static final int VIEW_TYPE_ROOM_MESSAGE_VOICE = 22;
    public static final int VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS = 23;
    public static final int VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS_TOO_LARGE = 24;
    public static final int VIEW_TYPE_ROOM_MESSAGE_MULTI_ATTACHMENTS = 25;
    private static Thread thread;
    private final LayoutInflater layoutInflater;

    private final String selfId;
    private final DiraActivity context;
    private final List<AttachmentsStorageListener> listeners = new ArrayList<>();
    private final CacheUtils cacheUtils;
    private final String serverAddress;
    private final RecyclerView recyclerView;
    private final long maxAutoLoadSize;
    private final DiraMediaPlayer diraMediaPlayer = new DiraMediaPlayer();
    private final MessageAdapterListener messageAdapterListener;
    private final Amplituda amplituda;
    private final Executor voiceWaveformsThread = Executors.newSingleThreadExecutor();
    private Room room;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();
    private MessageReplyListener replyClickedListener;

    public LegacyRoomMessagesAdapter(DiraActivity context,
                                     RecyclerView recyclerView,
                                     String serverAddress,
                                     Room room,
                                     MessageAdapterListener messageAdapterListener) {
        this.context = context;
        this.serverAddress = serverAddress;
        this.messageAdapterListener = messageAdapterListener;
        this.room = room;
        this.recyclerView = recyclerView;

        layoutInflater = LayoutInflater.from(context);
        cacheUtils = new CacheUtils(context);
        amplituda = new Amplituda(context);


        selfId = cacheUtils.getString(CacheUtils.ID);
        maxAutoLoadSize = cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE);
    }

    public void setReplyClickedListener(MessageReplyListener replyClickedListener) {
        this.replyClickedListener = replyClickedListener;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getSelfId() {
        return selfId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public LegacyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Timer timer = new Timer("OnCreateViewHolder");
        LegacyViewHolder holder;
        View view;
        if (viewType >= VIEW_TYPE_ROOM_MESSAGE) {
            view = layoutInflater.inflate(R.layout.room_message, parent, false);
        } else {
            view = layoutInflater.inflate(R.layout.self_message, parent, false);
        }

        holder = new LegacyViewHolder(view);

        LegacyViewHolder legacyViewHolder = displayMessageView(holder, viewType);
        timer.reportTime();
        return legacyViewHolder;
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
    public void onViewRecycled(@NonNull LegacyViewHolder holder) {
        super.onViewRecycled(holder);
        DiraVideoPlayer player = null;
        if (holder.videoPlayer != null) player = holder.videoPlayer;
        if (holder.bubblePlayer != null) player = holder.bubblePlayer;

        if (player != null) {
            player.reset();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull LegacyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        DiraVideoPlayer player = null;
        if (holder.videoPlayer != null) player = holder.videoPlayer;
        if (holder.bubblePlayer != null) player = holder.bubblePlayer;

        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull LegacyViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        DiraVideoPlayer player = null;
        if (holder.videoPlayer != null) player = holder.videoPlayer;
        if (holder.bubblePlayer != null) player = holder.bubblePlayer;


        if (player != null) {
            if (player.getState() == DiraVideoPlayerState.PAUSED) {
                player.play();
            }

        }
    }

    @Override
    public void onBindViewHolder(@NonNull LegacyViewHolder holder, int position) {


        Message message = messages.get(position);

        if (holder.attachmentsStorageListener != null) {
            AttachmentsStorage.removeAttachmentsStorageListener(holder.attachmentsStorageListener);
            listeners.remove(holder.attachmentsStorageListener);
        }


        // holder.loading.setVisibility(View.GONE);

        if (holder.imageContainer != null) {
            holder.videoPlayer.reset();
            holder.imageView.setVisibility(View.GONE);
            holder.videoPlayer.setVisibility(View.GONE);
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

        holder.timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime()));
    }

    public void updateAttachment(LegacyViewHolder holder, Attachment attachment, File file, Message message) {

        int attachmentsCount = message.getAttachments().size();
        if (message.getAttachments().size() > 1) {

            int position = 0;
            for (Attachment attachment1 : message.getAttachments()) {
                if (attachment.equals(attachment1)) {
                    position = message.getAttachments().indexOf(attachment1);
                    break;
                }
            }

            // renew bitmap

        } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
            holder.imageView.setVisibility(View.VISIBLE);
            DiraActivity.runGlobalBackground(() -> {
                Bitmap bitmap = AppStorage.getBitmapFromPath(file.getPath(), context);
                context.runOnUiThread(() -> {
                    holder.imageView.setImageBitmap(bitmap);
                });
            });

            // Causing "blink"
            // Picasso.get().load(Uri.fromFile(file)).into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreviewActivity.open(context, file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO, holder.imageContainer);
                }
            });
            // holder.loading.setVisibility(View.GONE);
        } else if (attachment.getAttachmentType() == AttachmentType.VIDEO || attachment.getAttachmentType() == AttachmentType.BUBBLE) {


            DiraVideoPlayer videoPlayer = holder.videoPlayer;

            if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                videoPlayer = holder.bubblePlayer;
                videoPlayer.attachDebugIndicator(holder.imageView);
            } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                holder.imageView.setVisibility(View.VISIBLE);


                videoPlayer.attachDebugIndicator(holder.viewsContainer);
                videoPlayer.setVisibility(View.VISIBLE);
                DiraVideoPlayer finalVideoPlayer = videoPlayer;
                holder.imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                                (holder.imageView.getMeasuredWidth(), holder.imageView.getMeasuredHeight());
                        finalVideoPlayer.setLayoutParams(params);


                        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                        alphaAnimation.setDuration(500);

                        alphaAnimation.setFillAfter(true);
                        finalVideoPlayer.startAnimation(alphaAnimation);
                    }
                });

            }
            DiraVideoPlayer finalVideoPlayer = videoPlayer;

            videoPlayer.attachRecyclerView(recyclerView);
            videoPlayer.attachDiraActivity(context);

            videoPlayer.play(file.getPath());


            try {


                holder.loading.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

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


                            diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {

                                    diraMediaPlayer.start();
                                    holder.timeText.setText(AppStorage.getStringSize(attachment.getSize()));
                                    ((DiraVideoPlayer) v).setSpeed(1f);
                                    ((DiraVideoPlayer) v).setProgress(0);
                                    diraMediaPlayer.setOnPreparedListener(null);


                                }
                            });
                            diraMediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
            // holder.loading.setVisibility(View.GONE);


            voiceWaveformsThread.execute(() -> {


                try {


                    // Check if media file is not corrupted
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(context, Uri.fromFile(file));
                    String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                    if (dur.equals("0")) return;

                    amplituda.processAudio(file)
                            .get(result -> {
                                try {
                                    List<Integer> amplitudesData = result.amplitudesAsList();
                                    int[] array = new int[amplitudesData.size()];
                                    for (int i = 0; i < amplitudesData.size(); i++)
                                        array[i] = amplitudesData.get(i);


                                    context.runOnUiThread(() -> {
                                        try {
                                            holder.waveformSeekBar.setSampleFrom(array);
                                        } catch (Exception ignored) {
                                        }
                                    });
                                } catch (Exception e) {

                                }
                            }, exception -> {

                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });


            holder.waveformSeekBar.setProgress(attachment.getVoiceMessageStopProgress());
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
                                 boolean isSameDay, boolean isSameYear, LegacyViewHolder holder) {
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
        } else if (StringFormatter.isEmoji(message.getText()) && StringFormatter.getEmojiCount(message.getText()) < 4) {
            holder.messageContainer.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.VISIBLE);
            holder.emojiText.setText(message.getText());
        } else {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.emojiText.setVisibility(View.GONE);
            holder.messageText.setText(message.getText());
        }


        if (holder.imageContainer != null) {
            if (message.getAttachments().size() == 1) {
                Attachment attachment = message.getAttachments().get(0);

                if (attachment.getWidth() > 0 & attachment.getHeight() > 0) {

                    DiraActivity.runGlobalBackground(() -> {
                        Bitmap previewBitmap = attachment.getBitmapPreview();
                        if (previewBitmap == null) {
                            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                            Bitmap bmp = Bitmap.createBitmap(attachment.getWidth(), attachment.getHeight(), conf);
                            context.runOnUiThread(() -> {
                                holder.imageView.setImageBitmap(bmp);
                            });
                        } else {
                            context.runOnUiThread(() -> {
                                holder.imageView.setImageBitmap(previewBitmap);
                            });

                        }
                    });


                }
            }
        }
        loadMessageAttachment(message, holder);
        if (!isSelfMessage) {
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
            if (members.containsKey(message.getAuthorId())) {

                Member member = members.get(message.getAuthorId());
                if (holder.nicknameText != null) {
                    holder.nicknameText.setText(member.getNickname());
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
                if (showProfilePicture) {

                    holder.nicknameText.setText(message.getAuthorNickname());
                    holder.nicknameText.setVisibility(View.VISIBLE);
                }
            }


        } else if (message.getMessageReadingList() != null) {
            if (message.getMessageReadingList().size() == 0) {
                holder.messageBackground.getBackground().setColorFilter(
                        Theme.getColor(context, R.color.unread_message_background), PorterDuff.Mode.SRC_IN);
            } else {
                holder.messageBackground.getBackground().setColorFilter(
                        Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            }
        }

        fillMessageReply(message.getRepliedMessage(), holder, isSelfMessage);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void bindRoomUpdateMessage(Message message, LegacyViewHolder holder) {
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
            holder.roomUpdatesIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_room_updates));
            holder.roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage(holder);
        } else if (message.getCustomClientData() instanceof RoomNameChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_name_change));

            holder.roomUpdatesIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_room_updates));
            holder.roomUpdatesText.setText(((RoomNameChangeClientData) message.getCustomClientData()).getOldName());
            applyDefaultIconOnUpdateMessage(holder);
            holder.roomUpdatesText.setVisibility(View.VISIBLE);
        } else if (message.getCustomClientData() instanceof RoomIconChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_picture_change));

            String path = ((RoomIconChangeClientData) message.getCustomClientData()).getImagePath();
            setImageOnRoomUpdateMessage(holder, path);

            holder.roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage(holder);
        } else if (message.getCustomClientData() instanceof RoomNameAndIconChangeClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_name_and_picture_change));

            holder.roomUpdatesText.setText(((RoomNameAndIconChangeClientData) message.getCustomClientData()).getOldName());

            String path = ((RoomNameAndIconChangeClientData) message.getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(holder, path);

            holder.roomUpdatesText.setVisibility(View.VISIBLE);
        } else if (message.getCustomClientData() instanceof KeyGenerateStartClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.key_generate_start));
            holder.roomUpdatesIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_encryption));
            holder.roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage(holder);
        } else if (message.getCustomClientData() instanceof KeyGeneratedClientData) {
            holder.roomUpdatesMainText.setText(((KeyGeneratedClientData) message.
                    getCustomClientData()).getClientDataText(context));

            if (Objects.equals(((KeyGeneratedClientData) message.
                    getCustomClientData()).getResult(), KeyGeneratedClientData.RESULT_CANCELLED)) {
                holder.roomUpdatesIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_encryption_disabled));
            } else {
                holder.roomUpdatesIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_encryption));
            }
            holder.roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage(holder);
        }
    }

    private void applyDefaultIconOnUpdateMessage(LegacyViewHolder holder) {
        holder.roomUpdatesIcon.setImageTintList(ColorStateList.valueOf(
                Theme.getColor(context, R.color.client_data_icon_color)));
        holder.roomUpdatesIcon.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
    }

    private void setImageOnRoomUpdateMessage(LegacyViewHolder holder, String path) {

        if (path == null) {
            holder.roomUpdatesIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.placeholder));
            holder.roomUpdatesIcon.setColorFilter(Theme.getColor(context, R.color.accent));
            holder.roomUpdatesIcon.getBackground().setColorFilter(
                    Theme.getColor(context, R.color.dark), PorterDuff.Mode.SRC_IN);
        } else {
            Picasso.get().load(new File(path)).into(holder.roomUpdatesIcon);
            holder.roomUpdatesIcon.setImageTintList(null);
            holder.roomUpdatesIcon.setColorFilter(null);
        }
    }

    private void loadMessageAttachment(Message message, LegacyViewHolder holder) {
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

                                    //holder.loading.setVisibility(View.GONE);
                                    File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                                    if (file != null) {
                                        updateAttachment(holder, attachment, file, message);
                                    } else {
                                        // holder.loading.setVisibility(View.VISIBLE);

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
                            Logger.logDebug(this.getClass().getSimpleName(),
                                    "Attachment failed to download! Url:" + attachment.getFileUrl());
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // holder.loading.setVisibility(View.GONE);
                                    holder.imageView.setImageResource(R.drawable.ic_trash);
                                    holder.imageView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                };

                AttachmentsStorage.addAttachmentsStorageListener(holder.attachmentsStorageListener);
                listeners.add(holder.attachmentsStorageListener);

                long attachmentSize = 0;
                for (Attachment attachment : message.getAttachments()) {
                    attachmentSize += attachment.getSize();
                }

                int attachmentCount = message.getAttachments().size();
                for (int i = 0; i < attachmentCount; i++) {
                    Attachment attachment = message.getAttachments().get(i);

                    if (holder.imageView != null) {

                        holder.imageView.setVisibility(View.VISIBLE);
                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                        Bitmap bmp = Bitmap.createBitmap(attachment.getWidth(), attachment.getHeight(), conf);

                        holder.imageView.setImageBitmap(bmp);

                        DiraActivity.runGlobalBackground(() -> {

                            Bitmap previewBitmap = attachment.getBitmapPreview();
                            if (previewBitmap == null) {


                            } else {
                                context.runOnUiThread(() -> {
                                    holder.imageView.setImageBitmap(previewBitmap);
                                });
                            }
                        });

                    } else if (i == 0 & attachmentCount > 1) {
                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;

                        final ArrayList<AsymmetricItem> items = new ArrayList<>(attachmentCount);
                        for (int j = 0; j < attachmentCount; j++) {
                            Attachment attachment1 = message.getAttachments().get(j);
                            Bitmap bmp = Bitmap.createBitmap(attachment1.getWidth(),
                                    attachment1.getHeight(), conf);

                            // items.add()

                            //here need to inputBitmap into AsymmetricGridView
                        }

                        // there need to setup adapter
                        //ListAdapter adapter = new ListAdapter() {}
                    } else {
                        // holder.loading.setVisibility(View.VISIBLE);
                    }


                    File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                    if (file != null && !AttachmentsStorage.isAttachmentSaving(attachment)) {

                        updateAttachment(holder, attachment, file, message);
                    } else {
                        if (attachmentSize > maxAutoLoadSize) {
                            if (i == 0) {
                                // holder.loading.setVisibility(View.GONE);
                                holder.sizeText.setText(AppStorage.getStringSize(attachmentSize));
                                String finalEncryptionKey = encryptionKey;
                                long finalAttachmentSize = attachmentSize;
                                holder.buttonDownload.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        holder.buttonDownload.setVisibility(View.GONE);
                                        //holder.loading.setVisibility(View.VISIBLE);

                                        thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                final int[] finalProgress = {0};
                                                for (Attachment attachment1 : message.getAttachments()) {
                                                    try {
                                                        File savedFile = AttachmentsStorage.saveAttachment(context, attachment1, message.getRoomSecret(), false, new DownloadHandler() {
                                                            @Override
                                                            public void onProgressChanged(int progress) {

                                                                double thisAttachmentPart = (double) attachment.getSize() / finalAttachmentSize;

                                                                context.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        int currentDownloaded = ((int) (((double) progress) * thisAttachmentPart));

                                                                        currentDownloaded += finalProgress[0];

                                                                        if (progress == 100) {
                                                                            finalProgress[0] += ((int) 100d * thisAttachmentPart);
                                                                        }

                                                                        holder.sizeText.setText(AppStorage.getStringSize(finalAttachmentSize) + " (" + currentDownloaded + "%)");
                                                                    }
                                                                });

                                                            }
                                                        }, serverAddress, finalEncryptionKey);
                                                        context.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    int type = defineMessageType(message, selfId.equals(message.getAuthorId()));

                                                                    holder.viewsContainer.removeAllViews();
                                                                    displayMessageView(holder, type);
                                                                    holder.viewsContainer.requestLayout();

                                                                    if (type == VIEW_TYPE_SELF_MESSAGE_BUBBLE || type == VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
                                                                        holder.bubbleViewContainer.requestLayout();
                                                                    }

                                                                    // maybe also
                                                                    updateAttachment(holder, attachment1, savedFile, message);

                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                        thread.start();
                                    }
                                });
                            }
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
    }


    private void createBalloon(Message message, View view) {
        if (message.getMessageReadingList() == null) return;
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
        LinearLayout copyRow = layout.findViewById(R.id.copy_row);
        LinearLayout countRow = layout.findViewById(R.id.count_row);

        int size = userReadMessages.size();
        CardView firstCard = countRow.findViewById(R.id.card_view_1);
        CardView secondCard = countRow.findViewById(R.id.card_view_2);
        ImageView firstUserIcon = countRow.findViewById(R.id.icon_user_1);
        ImageView secondUserIcon = countRow.findViewById(R.id.icon_user_2);
        TextView countTextView = countRow.findViewById(R.id.count_row_text);

        boolean needCopyRow = message.getText().length() > 0;
        if (!needCopyRow) copyRow.setVisibility(View.GONE);

        String countText = "";
        if (size == 0) {
            firstCard.setVisibility(View.GONE);
            secondCard.setVisibility(View.GONE);
            if (message.getAuthorId().equals(selfId)) {
                countText = context.getString(R.string.message_tooltip_zero_read);
            } else {
                countText = context.getString(R.string.message_tooltip_only_you_read);
            }
        } else if (size == 1) {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            secondCard.setVisibility(View.GONE);
            countText = context.getString(R.string.message_tooltip_one_read).
                    replace("%s", String.valueOf(size));
        } else {
            if (userReadMessages.get(0).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(0).getPicturePath())).into(firstUserIcon);
            } else {
                firstUserIcon.setImageResource(R.drawable.placeholder);
            }
            if (userReadMessages.get(1).getPicturePath() != null) {
                Picasso.get().load(new File(userReadMessages.get(1).getPicturePath())).into(secondUserIcon);
            } else {
                secondUserIcon.setImageResource(R.drawable.placeholder);
            }
            countText = context.getString(R.string.message_tooltip_read_count).
                    replace("%s", String.valueOf(size));
        }
        countTextView.setText(countText);

        // Create balloon with list of reads
        if (size > 0) {
            Balloon listBalloon = new Balloon.Builder(context).
                    setLayout(R.layout.message_actions_tooltip_list)
                    .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                    .setIsVisibleArrow(false)
                    .build();

            RadiusLayout listLayout = (RadiusLayout) listBalloon.getContentView();
            listLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.tooltip_drawable));

            RelativeLayout recyclerLayout = listLayout.findViewById(R.id.recycler_layout);
            RecyclerView recyclerView = listLayout.findViewById(R.id.message_tooltip_recycler);

            LinearLayout countListRow = listLayout.findViewById(R.id.count_row);

            TextView countListTextView = listLayout.findViewById(R.id.count_row_text);
            countListTextView.setText(countText);

            if (size > 4) {
                int height = 4 * 48;
                height = Numbers.dpToPx(height, context);

                recyclerLayout.getLayoutParams().height = height;
                recyclerLayout.requestLayout();
            }

            MessageTooltipAdapter adapter = new MessageTooltipAdapter(context, userReadMessages);
            recyclerView.setAdapter(adapter);

            countListRow.setOnClickListener((View v) -> {
                listBalloon.dismiss();
                balloon.showAlignBottom(view);
            });

            countRow.setOnClickListener((View v) -> {
                balloon.dismiss();
                listBalloon.showAlignBottom(view);
            });

            listBalloon.setOnBalloonOutsideTouchListener((new OnBalloonOutsideTouchListener() {
                @Override
                public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    listBalloon.dismiss();
                }
            }));
        }

        copyRow.setOnClickListener((View v) -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", message.getText());
            clipboard.setPrimaryClip(clip);
            balloon.dismiss();
        });

        balloon.setOnBalloonOutsideTouchListener((new OnBalloonOutsideTouchListener() {
            @Override
            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                balloon.dismiss();
            }
        }));

        balloon.showAlignBottom(view);
    }

    private int defineMessageType(Message message, boolean isSelfMessage) {
        int type = VIEW_TYPE_SELF_MESSAGE;
        if (message.getCustomClientData() != null) {
            type = VIEW_TYPE_CLIENT_DATA;
        } else if (message.getAttachments().size() != 0) {

            boolean isAttachmentTooLarge = false;
            long attachmentsSize = 0;
            for (Attachment attachment : message.getAttachments()) {
                attachmentsSize += attachment.getSize();
                if (attachmentsSize > maxAutoLoadSize) {
                    if (AttachmentsStorage.getFileFromAttachment(attachment,
                            context, message.getRoomSecret()) == null) {
                        type = VIEW_TYPE_SELF_MESSAGE_ATTACHMENTS_TOO_LARGE;
                        isAttachmentTooLarge = true;
                    }
                }
            }

            if (!isAttachmentTooLarge) {
                if (message.getAttachments().size() > 1) {
                    type = VIEW_TYPE_SELF_MESSAGE_MULTI_ATTACHMENTS;
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

    private LegacyViewHolder displayMessageView(LegacyViewHolder holder, int viewType) {
        boolean isSelfMessage = viewType < VIEW_TYPE_ROOM_MESSAGE;

        if (isSelfMessage) viewType += VIEW_TYPE_ROOM_MESSAGE;

        View view = null;

        boolean bubbleAdded = false;
        MessageReplyComponent replyComponent;

        if (viewType == VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
            replyComponent = new MessageReplyComponent(context,
                    VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage);
            holder.bubbleViewContainer.addView(replyComponent);

            CardView bubble = new BubbleMessageView(context);
            holder.bubbleViewContainer.addView(bubble);

            holder.messageContainer.setVisibility(View.GONE);
            holder.viewsContainer.setVisibility(View.GONE);

            bubbleAdded = true;
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_VOICE) {
            view = new VoiceMessageView(context, isSelfMessage);
            holder.voiceLayout = (LinearLayout) view;
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS_TOO_LARGE) {
            view = new MessageAttachmentToLargeView(context, isSelfMessage);
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS) {
            view = new RoomMessageVideoPlayer(context);
        } else if (viewType == VIEW_TYPE_CLIENT_DATA) {
            view = new RoomMessageCustomClientDataView(context);
        } else if (viewType == VIEW_TYPE_ROOM_MESSAGE_MULTI_ATTACHMENTS) {
            view = MultiAttachmentMessageView.createNewMultiAttachmentView(context);
        }

        if (!bubbleAdded) {
            replyComponent = new MessageReplyComponent(context,
                    viewType, isSelfMessage);
            holder.viewsContainer.addView(replyComponent);
        }

        if (!bubbleAdded & view != null) {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.viewsContainer.addView(view);
        }

        holder.updateViews();

        return holder;
    }

    private void fillMessageReply(Message message, LegacyViewHolder legacyViewHolder, boolean isSelfMessage) {
        if (message == null) {
            legacyViewHolder.replyContainer.setVisibility(View.GONE);
            return;
        }

        String text = "";
        Attachment attachment = null;
        boolean showImage = false;
        int size = message.getAttachments().size();
        if (size > 0) {
            attachment = message.getAttachments().get(0);
        }

        int textColorId = 0;
        if (isSelfMessage) textColorId = R.color.self_message_color;
        else textColorId = R.color.message_color;

        if (attachment != null) {
            int statusTextColor = 0;
            if (isSelfMessage) statusTextColor = R.color.self_reply_color;
            else statusTextColor = R.color.message_reply_color;
            legacyViewHolder.replyText.setTextColor(Theme.getColor(context, statusTextColor));
            if (size > 1) {
                text = context.getResources().getString(R.string.message_type_attachments);
            } else if (attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                text = context.getResources().getString(R.string.message_type_bubble);
            } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                text = context.getResources().getString(R.string.message_type_video);
            } else if (attachment.getAttachmentType() == AttachmentType.VOICE) {
                text = context.getResources().getString(R.string.message_type_voice);
            } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                text = message.getText();
                if (text == null | "".equals(text)) {
                    text = context.getResources().getString(R.string.message_type_image);
                } else {
                    legacyViewHolder.replyText.setTextColor(Theme.getColor
                            (context, textColorId));
                }

                File file = AttachmentsStorage.getFileFromAttachment(attachment,
                        context, room.getSecretName());

                if (file != null) {
                    Picasso.get().load(file).into(legacyViewHolder.replyImage);
                    legacyViewHolder.replyImageCard.setVisibility(View.VISIBLE);
                }
                showImage = true;
            }
        } else {
            text = message.getText();
            if (text == null) text = "";
            legacyViewHolder.replyText.setTextColor(Theme.getColor
                    (context, textColorId));
        }

        if (!showImage) {
            legacyViewHolder.replyImageCard.setVisibility(View.GONE);
        }

        String author = "";
        boolean isUnknown = true;
        if (message.getAuthorId().equals(selfId)) {
            author = context.getString(R.string.you);
            isUnknown = false;
        } else if (members != null) {
            Member member = members.get(message.getAuthorId());
            if (member != null) {
                author = member.getNickname();
                isUnknown = false;
            }
        }

        if (isUnknown) {
            author = context.getString(R.string.unknown);
        }

        legacyViewHolder.replyAuthor.setText(author);
        legacyViewHolder.replyText.setText(text);

        legacyViewHolder.replyContainer.setVisibility(View.VISIBLE);

        legacyViewHolder.replyContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (replyClickedListener != null) replyClickedListener.onClicked(message);
            }
        });
    }

    public interface MessageAdapterListener {
        void onFirstItemScrolled(Message message, int index);

        default void onLastLoadedScrolled(Message message, int index) {
        }

    }

}
