package com.diraapp.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageAttachmentLoader {

    private final long maxAutoLoadSize;

    private final List<AttachmentsStorageListener> listeners = new ArrayList<>();

    private final Room room;

    private final Context context;

    public MessageAttachmentLoader(Room room, Context context) {
        this.room = room;
        this.context = context;

        maxAutoLoadSize = new CacheUtils(context).getLong(CacheUtils.AUTO_LOAD_SIZE);
    }

    public void loadMessageAttachment(Message message, AttachmentViewHolder holder) {
        MessageAttachmentStorageListener listener = new MessageAttachmentStorageListener(holder, message);
        holder.setAttachmentStorageListener(listener);

        AttachmentDownloader.addAttachmentsStorageListener(listener);
        listeners.add(listener);

        long attachmentsSize = 0;
        for (Attachment attachment : message.getAttachments()) {
            if (attachment != null) {
                attachmentsSize += attachment.getSize();
            }
        }

        int attachmentCount = message.getAttachments().size();
        for (int i = 0; i < attachmentCount; i++) {
            Attachment attachment = message.getAttachments().get(i);

            File file = AttachmentDownloader.getFileFromAttachment(attachment, context, message.getRoomSecret());

            if (file != null && !AttachmentDownloader.isAttachmentSaving(attachment)) {

                holder.onAttachmentLoaded(attachment, file, message);
            } else {
                if (attachmentsSize > maxAutoLoadSize) {
                    // notify that AttachmentToLarge
                } else {
                    if (!AttachmentDownloader.isAttachmentSaving(attachment)) {
                        SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                        AttachmentDownloader.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());
                    }
                }

            }

        }
    }

    public void removeListener(AttachmentsStorageListener listener) {
        listeners.remove(listener);
        AttachmentDownloader.removeAttachmentsStorageListener(listener);
    }

    public void release() {
        for (AttachmentsStorageListener attachmentsStorageListener : listeners) {
            AttachmentDownloader.removeAttachmentsStorageListener(attachmentsStorageListener);
        }
    }


    public class MessageAttachmentStorageListener implements AttachmentsStorageListener {

        private final Message message;
        private AttachmentHolder holder;

        public MessageAttachmentStorageListener(AttachmentHolder holder, Message message) {
            this.holder = holder;
            this.message = message;
        }

        @Override
        public void onAttachmentBeginDownloading(Attachment attachment) {

        }

        @Override
        public void onAttachmentDownloaded(Attachment attachment) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                    boolean isMessageAttachment = false;
                    for (Attachment messageAttachment : message.getAttachments()) {
                        if (messageAttachment != null)
                            if (attachment.getFileUrl().equals(messageAttachment.getFileUrl()))
                                isMessageAttachment = true;
                    }
                    if (isMessageAttachment) {

                        File file = AttachmentDownloader.getFileFromAttachment(attachment, context, message.getRoomSecret());

                        if (file != null) {
                            if (holder != null) {
                                holder.onAttachmentLoaded(attachment, file, message);
                            }
                        } else {
                            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                            AttachmentDownloader.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());
                        }

                    }
                }
            });

        }

        @Override
        public void onAttachmentDownloadFailed(Attachment attachment) {
            boolean isMessageAttachment = false;
            for (Attachment messageAttachment : message.getAttachments()) {
                if (messageAttachment != null)
                    if (attachment.getFileUrl().equals(messageAttachment.getFileUrl()))
                        isMessageAttachment = true;
            }
            if (isMessageAttachment) {
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Attachment failed to download! Url:" + attachment.getFileUrl());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.onLoadFailed(attachment);
                        }
                    }
                });
            }

        }

        public void removeViewHolder() {
            AttachmentDownloader.removeAttachmentsStorageListener(this);
            holder = null;
        }
    }

    public interface AttachmentHolder {
        void onAttachmentLoaded(Attachment attachment, File file, Message message);

        void onLoadFailed(Attachment attachment);
    }

}
