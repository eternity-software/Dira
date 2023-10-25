package com.diraapp.ui.adapters.messages.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorage;
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

    public MessageAttachmentLoader(Room room, Context activity) {
        this.room = room;
        this.context = activity;

        maxAutoLoadSize = new CacheUtils(activity).getLong(CacheUtils.AUTO_LOAD_SIZE);
    }

    public void loadMessageAttachment(Message message, AttachmentViewHolder holder) {
        String encryptionKey = "";
        if (room != null) {
            if (message.getLastTimeEncryptionKeyUpdated() == room.getTimeEncryptionKeyUpdated()) {
                encryptionKey = room.getEncryptionKey();
            }
        }
        MessageAttachmentStorageListener listener = new MessageAttachmentStorageListener(holder, message);
        holder.setAttachmentStorageListener(listener);

        AttachmentsStorage.addAttachmentsStorageListener(listener);
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

            File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

            if (file != null && !AttachmentsStorage.isAttachmentSaving(attachment)) {

                holder.onAttachmentLoaded(attachment, file, message);
            } else {
                if (attachmentsSize > maxAutoLoadSize) {
                    // notify that AttachmentToLarge
                } else {
                    if (!AttachmentsStorage.isAttachmentSaving(attachment)) {
                        SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                        AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());
                    }
                }

            }

        }
    }

    public void removeListener(AttachmentsStorageListener listener) {
        listeners.remove(listener);
        AttachmentsStorage.removeAttachmentsStorageListener(listener);
    }

    public void release() {
        for (AttachmentsStorageListener attachmentsStorageListener : listeners) {
            AttachmentsStorage.removeAttachmentsStorageListener(attachmentsStorageListener);
        }
    }


    public class MessageAttachmentStorageListener implements AttachmentsStorageListener {

        private final Message message;
        private AttachmentViewHolder holder;

        public MessageAttachmentStorageListener(AttachmentViewHolder holder, Message message) {
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

                        File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

                        if (file != null) {
                            if (holder != null) {
                                holder.onAttachmentLoaded(attachment, file, message);
                            }
                        } else {
                            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment, message.getRoomSecret());
                            AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, room.getServerAddress());
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
            AttachmentsStorage.removeAttachmentsStorageListener(this);
            holder = null;
        }
    }

}
