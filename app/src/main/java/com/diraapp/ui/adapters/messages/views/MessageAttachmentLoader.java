package com.diraapp.ui.adapters.messages.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.diraapp.DiraApplication;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.felipecsl.asymmetricgridview.library.model.AsymmetricItem;

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

        long attachmentSize = 0;
        for (Attachment attachment : message.getAttachments()) {
            attachmentSize += attachment.getSize();
        }

        int attachmentCount = message.getAttachments().size();
        for (int i = 0; i < attachmentCount; i++) {
            Attachment attachment = message.getAttachments().get(i);

            File file = AttachmentsStorage.getFileFromAttachment(attachment, context, message.getRoomSecret());

            if (file != null && !AttachmentsStorage.isAttachmentSaving(attachment)) {

                holder.onAttachmentLoaded(attachment, file, message);
            } else {
                if (attachmentSize > maxAutoLoadSize) {
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

        private AttachmentViewHolder holder;

        private final Message message;

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
                    if (attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl())) {

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
            if (attachment.getFileUrl().equals(message.getAttachments().get(0).getFileUrl())) {
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Attachment failed to download! Url:" + attachment.getFileUrl());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder != null) {
                            holder.onLoadFailed();
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
