package com.diraapp.ui.adapters.messages.views.viewholders;

import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.PreviewActivity;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;

import java.io.File;
import java.io.IOException;

public class BubbleViewHolder extends AttachmentViewHolder {

    private DiraVideoPlayer bubblePlayer;
    private BubbleMessageView bubbleContainer;


    public BubbleViewHolder(@NonNull ViewGroup itemView,
                            MessageAdapterContract messageAdapterContract,
                            ViewHolderManagerContract viewHolderManagerContract,
                            boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

        setOuterContainer(true);
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        //bubblePlayer.attachDebugIndicator(imageView);
        getMessageAdapterContract().attachVideoPlayer(bubblePlayer);
        bubblePlayer.play(file.getPath());

        try {
            //loading.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bubblePlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiraMediaPlayer diraMediaPlayer = getViewHolderManagerContract().getDiraMediaPlayer();

                try {
                    if (diraMediaPlayer.isPlaying()) {
                        diraMediaPlayer.stop();
                    }
                    diraMediaPlayer.reset();
                    diraMediaPlayer.setDataSource(file.getPath());


                    diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            diraMediaPlayer.start();
                            //timeText.setText(AppStorage.getStringSize(attachment.getSize()));
                            ((DiraVideoPlayer) v).setSpeed(1f);
                            ((DiraVideoPlayer) v).setProgress(0);
                            diraMediaPlayer.setOnPreparedListener(null);


                        }
                    });
                    diraMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onLoadFailed() {

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
        if (!isInitialized) return;
        bubblePlayer.reset();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (!isInitialized) return;
        bubblePlayer.pause();
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        if (!isInitialized) return;
        if (bubblePlayer.getState() == DiraVideoPlayerState.PAUSED)
            bubblePlayer.play();
    }
}
