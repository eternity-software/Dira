package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class BubbleViewHolder extends BaseMessageViewHolder {

    DiraVideoPlayer bubblePlayer;
    BubbleMessageView bubbleContainer;

    public BubbleViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
        setOuterContainer(true);
    }

    @Override
    protected void postInflate() {
        super.postInflate();
        CardView bubble = new BubbleMessageView(itemView.getContext());
        bubbleContainer.addView(bubble);

        bubbleContainer = itemView.findViewById(BubbleMessageView.BUBBLE_CONTAINER_ID);
        bubblePlayer = itemView.findViewById(R.id.bubble_player);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
