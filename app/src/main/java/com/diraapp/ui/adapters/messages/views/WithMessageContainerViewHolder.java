package com.diraapp.ui.adapters.messages.views;

import android.view.View;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.components.MessageReplyComponent;

public abstract class WithMessageContainerViewHolder extends BaseMessageViewHolder {
    public WithMessageContainerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                getItemViewType(), isSelfMessage);

        postInflatedViewsContainer.addView(replyComponent);
        updateReplies();
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
