package com.diraapp.ui.adapters.messages.views;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.MessageReplyComponent;

public class TextMessageViewHolder extends BaseMessageViewHolder {

    TextView messageText;
    TextView emojiText;


    public TextMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        messageText = itemView.findViewById(R.id.message_text);
        emojiText = itemView.findViewById(R.id.emoji_view);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                getItemViewType(), isSelfMessage);
        postInflatedViewsContainer.addView(replyComponent);

        isInitialised = true;
        updateViews();
        updateReplies();
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
