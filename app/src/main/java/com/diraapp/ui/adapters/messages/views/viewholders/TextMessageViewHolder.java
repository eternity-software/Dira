package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterConfig;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;


public class TextMessageViewHolder extends BaseMessageViewHolder {


    public TextMessageViewHolder(@NonNull ViewGroup itemView, MessageAdapterConfig messageAdapterConfig) {
        super(itemView, messageAdapterConfig);
    }


    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
