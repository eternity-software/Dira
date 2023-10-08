package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;


public class TextMessageViewHolder extends BaseMessageViewHolder {


    public TextMessageViewHolder(@NonNull ViewGroup itemView,
                                 MessageAdapterContract messageAdapterContract,
                                 ViewHolderManagerContract viewHolderManagerContract,
                                 boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }


    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        messageText.setVisibility(View.VISIBLE);

    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        messageText.setText(message.getText());
    }
}
