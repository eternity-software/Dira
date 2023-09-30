package com.diraapp.ui.adapters.messages.views;

import static com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE;

import android.view.View;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.components.MessageReplyComponent;

public abstract class NoMessageContainerViewHolder extends BaseMessageViewHolder {
    public NoMessageContainerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage);
        bubbleContainer.addView(replyComponent);

        messageContainer.setVisibility(View.GONE);
        postInflatedViewsContainer.setVisibility(View.GONE);

        updateReplies();
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
