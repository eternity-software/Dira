package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;

public class EmojiMessageViewHolder extends BaseMessageViewHolder {
    private TextView emojiText;

    public EmojiMessageViewHolder(@NonNull ViewGroup itemView,
                                  MessageAdapterContract messageAdapterContract,
                                  ViewHolderManagerContract viewHolderManagerContract,
                                  boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

        setOuterContainer(true);
    }


    @Override
    protected void postInflate() {
        super.postInflate();
        emojiText = itemView.findViewById(R.id.emoji_view);
        emojiText.setVisibility(View.VISIBLE);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        emojiText.setText(message.getText());
    }
}