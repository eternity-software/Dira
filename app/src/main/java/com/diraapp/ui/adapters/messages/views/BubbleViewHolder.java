package com.diraapp.ui.adapters.messages.views;
import static com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class BubbleViewHolder extends BaseMessageViewHolder {

    DiraVideoPlayer bubblePlayer;
    BubbleMessageView bubbleContainer;

    public BubbleViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        bubbleContainer = itemView.findViewById(BubbleMessageView.BUBBLE_CONTAINER_ID);
        bubblePlayer = itemView.findViewById(R.id.bubble_player);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage);
        bubbleContainer.addView(replyComponent);

        CardView bubble = new BubbleMessageView(itemView.getContext());
        bubbleContainer.addView(bubble);

        messageContainer.setVisibility(View.GONE);
        postInflatedViewsContainer.setVisibility(View.GONE);

        isInitialised = true;
        updateViews();
        updateReplies();
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
