package com.diraapp.ui.adapters.messages.viewholders;

import static com.diraapp.ui.adapters.messages.RoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.BaseViewHolder;
import com.diraapp.ui.adapters.messages.ViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class BubbleViewHolder extends BaseViewHolder {

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
    public void onCreate() {
        super.onCreate();
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage());
        bubbleViewContainer.addView(replyComponent);

        CardView bubble = new BubbleMessageView(itemView.getContext());
        bubbleViewContainer.addView(bubble);

        messageContainer.setVisibility(View.GONE);
        viewsContainer.setVisibility(View.GONE);

        setInitialised(true);
        updateViews();
        updateReplies();
    }

    @Override
    public void onBind(Message message, Message previousMessage) {

    }
}
