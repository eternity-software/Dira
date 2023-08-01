package com.diraapp.ui.adapters.messages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;

import com.diraapp.api.processors.RoomUpdatesProcessor;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.MessageReadRequest;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.MessageDao;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;
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


    public static final int VIEW_TYPE_SELF_MESSAGE = 1;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 0;
    private static Thread thread;
    private final LayoutInflater layoutInflater;

    private final String selfId;
    private final Activity context;
    private Room room;

    private ColorTheme theme;
    private final List<AttachmentsStorageListener> listeners = new ArrayList<>();
    private final CacheUtils cacheUtils;
    private final HashMap<View, Integer> pendingAsyncOperations = new HashMap<>();
    private final HashMap<String, Bitmap> loadedBitmaps = new HashMap<>();
    private final String secretName;
    private final String serverAddress;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();

    private final long maxAutoLoadSize;

    private MessageAdapterListener messageAdapterListener;


    public interface MessageAdapterListener {
        void onFirstItemScrolled(Message message, int index);
    }

    public RoomMessagesAdapter(Activity context, String secretName, String serverAddress, Room room, MessageAdapterListener messageAdapterListener) {
        this.context = context;
        this.secretName = secretName;
        this.serverAddress = serverAddress;
        this.messageAdapterListener = messageAdapterListener;
        this.room = room;
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
        if (viewType == VIEW_TYPE_ROOM_MESSAGE) {
            return new ViewHolder(layoutInflater.inflate(R.layout.room_message, parent, false));
        } else {
            return new ViewHolder(layoutInflater.inflate(R.layout.self_message, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getCustomClientData() != null) {
            return VIEW_TYPE_ROOM_MESSAGE;
        }
        if (selfId.equals(message.getAuthorId())) {
            return VIEW_TYPE_SELF_MESSAGE;
        }
        return VIEW_TYPE_ROOM_MESSAGE;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.videoPlayer.release();
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.videoPlayer.release();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        if (holder.attachmentsStorageListener != null) {
            AttachmentsStorage.removeAttachmentsStorageListener(holder.attachmentsStorageListener);
            listeners.remove(holder.attachmentsStorageListener);
        }

        holder.messageText.setVisibility(View.VISIBLE);
        holder.videoPlayer.release();
        holder.videoPlayer.setDelay(10);

        holder.loading.setVisibility(View.GONE);
        holder.sizeContainer.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);
        holder.videoPlayer.setVisibility(View.GONE);
        holder.dateText.setVisibility(View.GONE);


        Message message = messages.get(position);

        if(position == messages.size() - 1)
        {
            messageAdapterListener.onFirstItemScrolled(message, position);
        }

        if (!message.isRead()) {
            message.setRead(true);

            // send ReadRequest
            MessageReadRequest request = new MessageReadRequest(selfId, System.currentTimeMillis(),
                    message.getId(), secretName);
            UpdateProcessor.getInstance().getRoomUpdatesProcessor().
                    addMessageToRequestList(request, serverAddress);
        }

        if (message.getText() == null) {
            holder.messageText.setVisibility(View.GONE);
        } else if (message.getText().length() == 0) {
            holder.messageText.setVisibility(View.GONE);
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
                    holder.videoPlayer.setVisibility(View.GONE);
                    Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                    Bitmap bmp = Bitmap.createBitmap(300, 300, conf);
                    holder.imageView.setImageBitmap(bmp);
                    Picasso.get().load(Uri.fromFile(file)).into(holder.imageView);
                    holder.imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PreviewActivity.class);
                            intent.putExtra(PreviewActivity.URI, file.getPath());
                            intent.putExtra(PreviewActivity.IS_VIDEO, attachment.getAttachmentType() == AttachmentType.VIDEO);
                            context.startActivity(intent);
                        }
                    });
                    holder.loading.setVisibility(View.GONE);
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
                    holder.videoPlayer.setVisibility(View.VISIBLE);
                    holder.imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.videoPlayer.getLayoutParams().height = holder.imageView.getMeasuredHeight();
                            holder.videoPlayer.getLayoutParams().width = holder.imageView.getMeasuredWidth();
                            holder.videoPlayer.requestLayout();

                            holder.videoPlayer.setVolume(0);

                            AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                            alphaAnimation.setDuration(500);

                            alphaAnimation.setFillAfter(true);
                            holder.videoPlayer.startAnimation(alphaAnimation);
                        }
                    });


                    try {
                        holder.videoPlayer.play(file.getPath());

                        holder.loading.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    holder.videoPlayer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PreviewActivity.class);
                            intent.putExtra(PreviewActivity.URI, file.getPath());
                            intent.putExtra(PreviewActivity.IS_VIDEO, attachment.getAttachmentType() == AttachmentType.VIDEO);
                            context.startActivity(intent);
                        }
                    });
                    holder.videoPlayer.setVideoPlayerListener(new VideoPlayer.VideoPlayerListener() {
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
                                holder.videoPlayer.play(file.getPath());
                                holder.videoPlayer.setVideoPlayerListener(null);
                                holder.videoPlayer.setVolume(0);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            holder.loading.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    public void unregisterListeners() {
        for (AttachmentsStorageListener attachmentsStorageListener : listeners) {
            AttachmentsStorage.removeAttachmentsStorageListener(attachmentsStorageListener);
        }
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
        if (holder.roomUpdatesLayout != null) {
            holder.roomUpdatesLayout.setVisibility(View.GONE);
        }

        holder.videoPlayer.setVolume(0);

        loadMessageAttachment(message, holder);

        boolean isSelfMessage = selfId.equals(
                message.getAuthorId());

        if (StringFormatter.isEmoji(message.getText()) && StringFormatter.getEmojiCount(message.getText()) < 3) {
            holder.messageContainer.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.VISIBLE);
            holder.emojiText.setText(message.getText());
        } else {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.emojiText.setVisibility(View.GONE);
            holder.messageText.setText(message.getText());

        }

        if (!isSelfMessage) {
            holder.nicknameText.setText(message.getAuthorNickname());
            holder.pictureContainer.setVisibility(View.VISIBLE);
            holder.nicknameText.setVisibility(View.VISIBLE);
            if (members.containsKey(message.getAuthorId())) {

                Member member = members.get(message.getAuthorId());
                holder.nicknameText.setText(member.getNickname());

                if (member.getImagePath() != null) {
                    Bitmap bitmap;
                    if (loadedBitmaps.containsKey(member.getImagePath())) {
                        bitmap = loadedBitmaps.get(member.getImagePath());
                    } else {
                        bitmap = AppStorage.getBitmapFromPath(member.getImagePath());
                        loadedBitmaps.put(member.getImagePath(), bitmap);
                    }
                    holder.profilePicture.setImageBitmap(bitmap);
                } else {
                    holder.profilePicture.setImageResource(R.drawable.placeholder);
                }

                if (previousMessage != null) {
                    if (previousMessage.getAuthorId() != null) {
                        if (previousMessage.getAuthorId().equals(message.getAuthorId()) && isSameDay
                                && isSameYear) {
                            holder.pictureContainer.setVisibility(View.INVISIBLE);
                            holder.nicknameText.setVisibility(View.GONE);
                        }
                    }
                }

            }
        }

        // apply color theme
        applyUserMessageColorTheme(holder, isSelfMessage);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void bindRoomUpdateMessage(Message message, ViewHolder holder) {
        holder.roomUpdatesLayout.setVisibility(View.VISIBLE);

        holder.nicknameText.setVisibility(View.GONE);
        holder.pictureContainer.setVisibility(View.GONE);
        holder.emojiText.setVisibility(View.GONE);
        holder.messageText.setVisibility(View.GONE);
        holder.roomUpdatesText.setVisibility(View.GONE);

        if (message.getCustomClientData() instanceof RoomJoinClientData) {
            holder.roomUpdatesMainText.setText(context.getString(R.string.room_update_new_member)
                    .replace("%s", ((RoomJoinClientData)
                            message.getCustomClientData()).getNewNickName()));

            String path = ((RoomJoinClientData) message.getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(holder, path);
            holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_room_updates));
            holder.roomUpdatesText.setVisibility(View.INVISIBLE);
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
        } else if (message.getCustomClientData() instanceof KeyGeneratedClientData) {
            holder.roomUpdatesMainText.setText(((KeyGeneratedClientData) message.
                    getCustomClientData()).getClientDataText(context));

            if(Objects.equals(((KeyGeneratedClientData) message.
                    getCustomClientData()).getResult(), KeyGeneratedClientData.RESULT_CANCELLED))
            {
                holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_encryption_disabled));
            }
            else
            {
                holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.ic_encryption));
            }
            applyDefaultIconOnUpdateMessage(holder);
        }

        applyRoomUpdateMessagesColorTheme(holder);
    }

    private void applyRoomUpdateMessagesColorTheme(ViewHolder holder) {
        holder.messageContainer.getBackground().setTint(theme.getMessageColor());
        holder.roomUpdatesMainText.setTextColor(theme.getTextColor());
        holder.roomUpdatesText.setTextColor(theme.getRoomUpdateMessageColor());
    }

    private void setImageOnRoomUpdateMessage(ViewHolder holder, String path) {
        Bitmap bitmap = AppStorage.getBitmapFromPath(path);
        if (bitmap == null) {
            holder.roomUpdatesIcon.setImageDrawable(context.getDrawable(R.drawable.placeholder));
            holder.roomUpdatesIcon.setImageTintList(null);
            holder.roomUpdatesIcon.getBackground().setTintList(ColorStateList.valueOf(context.
                    getResources().getColor(R.color.dark)));
        } else {
            holder.roomUpdatesIcon.setImageBitmap(bitmap);
            holder.roomUpdatesIcon.setImageTintList(null);
        }
    }

    private void applyDefaultIconOnUpdateMessage(ViewHolder holder) {
        holder.roomUpdatesIcon.setImageTintList(ColorStateList.valueOf(
                AppTheme.getInstance().getColorTheme().getTextColor()));
        holder.roomUpdatesIcon.getBackground().setTintList(ColorStateList.valueOf(
                AppTheme.getInstance().getColorTheme().getMessageColor()));
    }

    private void applyUserMessageColorTheme(ViewHolder holder, boolean isSelfMessage) {
        if (isSelfMessage) {
            holder.messageText.setTextColor(theme.getSelfTextColor());
            holder.messageText.setLinkTextColor(theme.getSelfLinkColor());
            holder.messageContainer.getBackground().setTint(theme.getSelfMessageColor());
        } else {
            holder.messageText.setTextColor(theme.getTextColor());
            holder.messageText.setLinkTextColor(theme.getRoomLickColor());
            holder.messageContainer.getBackground().setTint(theme.getMessageColor());
        }

        if (holder.sizeContainer.getVisibility() == View.VISIBLE) {
            if (isSelfMessage) {
                holder.sizeText.setTextColor(theme.getSelfLinkColor());
                holder.attachmentTooLargeText.setTextColor(theme.getSelfTextColor());
                holder.buttonDownload.getBackground().setTint(theme.getDownloadButtonColor());
                holder.buttonDownload.setTextColor(theme.getDownloadButtonColor());
            } else {
                holder.sizeText.setTextColor(theme.getRoomLickColor());
                holder.attachmentTooLargeText.setTextColor(theme.getTextColor());
                holder.buttonDownload.getBackground().setTint(theme.getDownloadButtonColor());
                holder.buttonDownload.setTextColor(theme.getDownloadButtonTextColor());
            }

        }

        if (holder.loading.getVisibility() == View.VISIBLE) {
            holder.attachmentProgressbar.setIndeterminateTintList(ColorStateList.
                    valueOf(theme.getSelfTextColor()));
        }
    }


    private void loadMessageAttachment(Message message, ViewHolder holder) {
        String encryptionKey = "";
        if(room != null)
        {
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
                        holder.buttonDownload.setVisibility(View.VISIBLE);
                        holder.sizeContainer.setVisibility(View.VISIBLE);
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
                                                        holder.sizeContainer.setVisibility(View.GONE);
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
}
