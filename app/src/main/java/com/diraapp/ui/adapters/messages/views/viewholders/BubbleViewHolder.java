package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterConfig;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;

public class BubbleViewHolder extends BaseMessageViewHolder {

    private DiraVideoPlayer bubblePlayer;
    private BubbleMessageView bubbleContainer;

    public BubbleViewHolder(@NonNull ViewGroup itemView, MessageAdapterConfig messageAdapterConfig) {
        super(itemView, messageAdapterConfig);
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

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        bubblePlayer.reset();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        bubblePlayer.pause();
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();

        if (bubblePlayer.getState() == DiraVideoPlayerState.PAUSED)
            bubblePlayer.play();
    }
}
