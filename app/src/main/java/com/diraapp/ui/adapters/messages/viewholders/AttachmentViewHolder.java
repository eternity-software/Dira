package com.diraapp.ui.adapters.messages.viewholders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.components.RoomMessageVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

public class AttachmentViewHolder extends TextMessageViewHolder {

    DiraVideoPlayer videoPlayer;
    ImageView imageView;
    CardView imageContainer;

    public AttachmentViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        super.updateViews();
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        imageContainer = itemView.findViewById(R.id.image_container);
    }

    @Override
    public void onCreate() {
        View view = new RoomMessageVideoPlayer(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        viewsContainer.addView(view);

        setInitialised(true);
        updateViews();
    }

    @Override
    public void onBind(Message message, Message previousMessage) {
        videoPlayer.reset();
        imageView.setVisibility(View.GONE);
        videoPlayer.setVisibility(View.GONE);
    }
}
