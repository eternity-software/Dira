package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.MessageAttachmentLoader;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;

import java.io.File;

public abstract class AttachmentViewHolder extends BaseMessageViewHolder {

    private MessageAttachmentLoader.MessageAttachmentStorageListener attachmentStorageListener = null;

    public AttachmentViewHolder(@NonNull ViewGroup itemView, MessageAdapterContract messageAdapterContract,
                                ViewHolderManagerContract viewHolderManagerContract, boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);
    }

    public MessageAttachmentLoader.MessageAttachmentStorageListener getAttachmentStorageListener() {
        return attachmentStorageListener;
    }

    public void setAttachmentStorageListener(MessageAttachmentLoader.MessageAttachmentStorageListener attachmentStorageListener) {
        this.attachmentStorageListener = attachmentStorageListener;
    }

    public void removeAttachmentStorageListener() {
        if (attachmentStorageListener != null) {
            attachmentStorageListener.removeViewHolder();
        }
        attachmentStorageListener = null;
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        getViewHolderManagerContract().getMessageAttachmentLoader()
                .loadMessageAttachment(message, (AttachmentViewHolder) this);
    }

    public abstract void onAttachmentLoaded(Attachment attachment, File file, Message message);

    public abstract void onLoadFailed();
}
