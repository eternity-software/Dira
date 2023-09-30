package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.NoMessageContainerViewHolder;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class BubbleViewHolder extends NoMessageContainerViewHolder {

    DiraVideoPlayer bubblePlayer;
    BubbleMessageView bubbleContainer;

    public BubbleViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

        CardView bubble = new BubbleMessageView(itemView.getContext());
        bubbleContainer.addView(bubble);

        bubbleContainer = itemView.findViewById(BubbleMessageView.BUBBLE_CONTAINER_ID);
        bubblePlayer = itemView.findViewById(R.id.bubble_player);
        isInitialised = true;
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
