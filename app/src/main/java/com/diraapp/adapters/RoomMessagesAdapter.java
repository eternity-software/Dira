package com.diraapp.adapters;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.activities.PreviewActivity;
import com.diraapp.appearance.AppTheme;
import com.diraapp.appearance.ColorTheme;
import com.diraapp.components.VideoPlayer;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
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

public class RoomMessagesAdapter extends RecyclerView.Adapter<RoomMessagesAdapter.ViewHolder> {


    public static final int VIEW_TYPE_SELF_MESSAGE = 1;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 0;
    private static Thread thread;
    private final LayoutInflater layoutInflater;
    private final Activity context;
    private final List<AttachmentsStorageListener> listeners = new ArrayList<>();
    private final CacheUtils cacheUtils;
    private final HashMap<View, Integer> pendingAsyncOperations = new HashMap<>();
    private final HashMap<String, Bitmap> loadedBitmaps = new HashMap<>();
    private final String secretName;
    private final String serverAddress;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();


    public RoomMessagesAdapter(Activity context, String secretName, String serverAddress) {
        this.context = context;
        this.secretName = secretName;
        this.serverAddress = serverAddress;
        layoutInflater = LayoutInflater.from(context);
        cacheUtils = new CacheUtils(context);
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
        if (cacheUtils.getString(CacheUtils.ID).equals(
                messages.get(position).getAuthorId()
        )) {
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
        if (message.getText().length() == 0) {
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


        holder.videoPlayer.setVolume(0);

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
                    if (attachment.getSize() > cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE)) {
                        holder.buttonDownload.setVisibility(View.VISIBLE);
                        holder.sizeContainer.setVisibility(View.VISIBLE);
                        holder.loading.setVisibility(View.GONE);
                        holder.sizeText.setText(AppStorage.getStringSize(attachment.getSize()));
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
                                                            holder.sizeText.setText(AppStorage.getStringSize(attachment.getSize()) + " (" + progress + "%)");
                                                        }
                                                    });

                                                }
                                            }, serverAddress);
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

        boolean isSelfMessage = cacheUtils.getString(CacheUtils.ID).equals(
                messages.get(position).getAuthorId());

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
                    if (previousMessage.getAuthorId().equals(message.getAuthorId()) && isSameDay) {
                        holder.pictureContainer.setVisibility(View.INVISIBLE);
                        holder.nicknameText.setVisibility(View.GONE);
                    }
                }

            }
        }


        // apply color theme
        ColorTheme theme = AppTheme.getInstance().getColorTheme();

        if (isSelfMessage) {
            holder.messageText.setTextColor(theme.getSelfTextColor());
            holder.messageContainer.getBackground().setTint(theme.getSelfMessageColor());
        } else {
            holder.messageText.setTextColor(theme.getTextColor());
            holder.messageContainer.getBackground().setTint(theme.getMessageColor());
        }

        if (holder.sizeContainer.getVisibility() == View.VISIBLE) {
            if (isSelfMessage) {
                holder.attachmentTooLargeText.setTextColor(theme.getSelfTextColor());
            } else {
                holder.attachmentTooLargeText.setTextColor(theme.getMessageColor());
            }

            holder.buttonDownload.getBackground().setTint(theme.getAccentColor());
            holder.buttonDownload.setTextColor(theme.getSelfTextColor());
        }

        if (holder.loading.getVisibility() == View.VISIBLE) {
            holder.attachmentProgressbar.setIndeterminateTintList(ColorStateList.
                    valueOf(theme.getSelfTextColor()));
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
                    retriever.setDataSource(file.getPath());
                    int width = 1;
                    int height = 1;
                    int rotation = 0;
                    try {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView emojiText;
        TextView nicknameText;
        TextView timeText;
        TextView buttonDownload;
        TextView sizeText;
        TextView dateText;
        ImageView profilePicture;
        ImageView imageView;
        VideoPlayer videoPlayer;
        CardView pictureContainer;
        LinearLayout messageContainer;
        LinearLayout sizeContainer;
        LinearLayout loading;

        TextView attachmentTooLargeText;

        AttachmentsStorageListener attachmentsStorageListener;

        ProgressBar attachmentProgressbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            nicknameText = itemView.findViewById(R.id.nickname_text);
            timeText = itemView.findViewById(R.id.time_view);
            buttonDownload = itemView.findViewById(R.id.download_button);
            sizeContainer = itemView.findViewById(R.id.attachment_too_large);
            emojiText = itemView.findViewById(R.id.emoji_view);
            sizeText = itemView.findViewById(R.id.size_view);
            dateText = itemView.findViewById(R.id.date_view);
            loading = itemView.findViewById(R.id.loading_attachment_layout);
            imageView = itemView.findViewById(R.id.image_view);
            videoPlayer = itemView.findViewById(R.id.video_player);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            pictureContainer = itemView.findViewById(R.id.picture_container);
            messageContainer = itemView.findViewById(R.id.message_container);
            attachmentTooLargeText = itemView.findViewById(R.id.attachment_too_large_text);
            attachmentProgressbar = itemView.findViewById(R.id.attachment_progressbar);
        }
    }
}
