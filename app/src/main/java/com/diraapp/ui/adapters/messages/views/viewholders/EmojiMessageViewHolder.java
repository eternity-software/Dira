package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterConfig;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;

public class EmojiMessageViewHolder extends BaseMessageViewHolder {
    private TextView emojiText;

    public EmojiMessageViewHolder(@NonNull ViewGroup itemView, MessageAdapterConfig messageAdapterConfig) {
        super(itemView, messageAdapterConfig);
        setOuterContainer(true);
    }


    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

        emojiText = itemView.findViewById(R.id.emoji_view);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
