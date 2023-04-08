package com.diraapp.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.diraapp.R;
import com.diraapp.activities.PreviewActivity;
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
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;

public class RoomMessagesAdapter extends RecyclerView.Adapter<RoomMessagesAdapter.ViewHolder> {


    public static final int VIEW_TYPE_SELF_MESSAGE = 1;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 0;
    private static Thread thread;
    private final LayoutInflater layoutInflater;
    private final Activity context;
    private List<Message> messages = new ArrayList<>();
    private List<AttachmentsStorageListener> listeners = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();


    public RoomMessagesAdapter(Activity context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
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
        if (CacheUtils.getInstance().getString(CacheUtils.ID, context).equals(
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

        if(holder.attachmentsStorageListener != null)
        {
            AttachmentsStorage.removeAttachmentsStorageListener(holder.attachmentsStorageListener);
            listeners.remove(holder.attachmentsStorageListener);
        }

        holder.messageText.setVisibility(View.VISIBLE);
        holder.videoPlayer.release();
        holder.videoPlayer.setDelay(300);
        holder.loading.setVisibility(View.GONE);
        holder.sizeContainer.setVisibility(View.GONE);
        holder.imageView.setVisibility(View.GONE);
        holder.videoPlayer.setVisibility(View.GONE);

        Message message = messages.get(position);
        if (message.getText().length() == 0) {
            holder.messageText.setVisibility(View.GONE);
        }
        Message previousMessage = null;


        if (position < messages.size() - 1) {
            previousMessage = messages.get(position + 1);
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
                                if(attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl()))
                                {

                                    File file = AppStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                                    if (file != null) {
                                        updateAttachment(holder, attachment, file);
                                    }

                                }
                            }
                        });

                    }

                    @Override
                    public void onAttachmentDownloadFailed(Attachment attachment) {
                        if(attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl())) {
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
                File file = AppStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                if (file != null) {

                    updateAttachment(holder, attachment, file);
                } else {

                    if (attachment.getSize() > CacheUtils.getInstance().getLong(CacheUtils.AUTO_LOAD_SIZE, context)) {
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
                                            });
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
                    }
                    else
                    {
                        if(!AttachmentsStorage.isAttachmentSaving(attachment))
                        {
                            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                            AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask);
                        }
                    }


                }


            }
        }

        boolean isSelfMessage = CacheUtils.getInstance().getString(CacheUtils.ID, context).equals(
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
                    holder.profilePicture.setImageBitmap(AppStorage.getImage(member.getImagePath()));
                } else {
                    holder.profilePicture.setImageResource(R.drawable.placeholder);
                }

                if (previousMessage != null) {
                    if (previousMessage.getAuthorId().equals(message.getAuthorId())) {
                        holder.pictureContainer.setVisibility(View.INVISIBLE);
                        holder.nicknameText.setVisibility(View.GONE);
                    }
                }

            }
        }


        holder.timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime(), context));
    }

    public void updateAttachment(ViewHolder holder, Attachment attachment, File file) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.loading.setVisibility(View.GONE);
                if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.videoPlayer.setVisibility(View.GONE);
                    holder.imageView.setImageBitmap(AppStorage.getImage(file.getPath()));
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
                    holder.imageView.setVisibility(View.GONE);
                    holder.videoPlayer.setVisibility(View.VISIBLE);

                    try {
                        holder.videoPlayer.play(file.getPath());
                        holder.videoPlayer.setVolume(0);
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
                        public void onReady() {
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

    public void unregisterListeners()
    {
        for(AttachmentsStorageListener attachmentsStorageListener : listeners)
        {
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
        ImageView profilePicture;
        ImageView imageView;
        VideoPlayer videoPlayer;
        CardView pictureContainer;
        LinearLayout messageContainer;
        LinearLayout sizeContainer;
        LinearLayout loading;

        AttachmentsStorageListener attachmentsStorageListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            nicknameText = itemView.findViewById(R.id.nickname_text);
            timeText = itemView.findViewById(R.id.time_view);
            buttonDownload = itemView.findViewById(R.id.download_button);
            sizeContainer = itemView.findViewById(R.id.attachment_too_large);
            emojiText = itemView.findViewById(R.id.emoji_view);
            sizeText = itemView.findViewById(R.id.size_view);
            loading = itemView.findViewById(R.id.loading_attachment_layout);
            imageView = itemView.findViewById(R.id.image_view);
            videoPlayer = itemView.findViewById(R.id.video_player);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            pictureContainer = itemView.findViewById(R.id.picture_container);
            messageContainer = itemView.findViewById(R.id.message_container);
        }
    }
}
