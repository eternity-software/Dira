package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;

import java.io.File;


public class TextMessageViewHolder extends AttachmentViewHolder {


    public TextMessageViewHolder(@NonNull ViewGroup itemView,
                                 MessageAdapterContract messageAdapterContract,
                                 ViewHolderManagerContract viewHolderManagerContract,
                                 boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }


    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        //messageText.setVisibility(View.VISIBLE);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        if (message.getText().length() == 0) {
            messageText.setVisibility(View.GONE);
            return;
        }

        messageText.setVisibility(View.VISIBLE);
        messageText.setText(message.getText());
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {

    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }
}
