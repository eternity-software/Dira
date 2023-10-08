package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.RoomMessageVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class AttachmentViewHolder extends BaseMessageViewHolder {

    private DiraVideoPlayer videoPlayer;
    private ImageView imageView;
    private CardView imageContainer;
    private TextView messageText;

    public AttachmentViewHolder(@NonNull ViewGroup itemView,
                                MessageAdapterContract messageAdapterContract,
                                boolean isSelfMessage) {
        super(itemView, messageAdapterContract, isSelfMessage);
    }

    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new RoomMessageVideoPlayer(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        imageContainer = itemView.findViewById(R.id.image_container);
        messageText = itemView.findViewById(R.id.message_text);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        videoPlayer.reset();
        imageView.setVisibility(View.GONE);
        videoPlayer.setVisibility(View.GONE);
    }
}
