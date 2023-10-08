package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;

public class BubbleViewHolder extends BaseMessageViewHolder {

    private DiraVideoPlayer bubblePlayer;
    private BubbleMessageView bubbleContainer;

    public BubbleViewHolder(@NonNull ViewGroup itemView,
                            MessageAdapterContract messageAdapterContract,
                            boolean isSelfMessage) {
        super(itemView, messageAdapterContract, isSelfMessage);
        setOuterContainer(true);
    }

    @Override
    protected void postInflate() {
        super.postInflate();
        CardView bubble = new BubbleMessageView(itemView.getContext());
      //  bubbleContainer = find(BubbleMessageView.BUBBLE_CONTAINER_ID);
        outerContainer.addView(bubble);
        bubblePlayer = find(R.id.bubble_player);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        if(!isInitialized) return;
        bubblePlayer.reset();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if(!isInitialized) return;
        bubblePlayer.pause();
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        if(!isInitialized) return;
        if (bubblePlayer.getState() == DiraVideoPlayerState.PAUSED)
            bubblePlayer.play();
    }
}
