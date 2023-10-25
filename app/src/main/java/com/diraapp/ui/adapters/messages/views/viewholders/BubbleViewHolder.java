package com.diraapp.ui.adapters.messages.views.viewholders;

import android.media.MediaPlayer;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.BuildConfig;
import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;

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
        if (file == null) return;
        bubblePlayer.play(file.getPath());

        try {
            //loading.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bubblePlayer.setOnClickListener(v -> {
            DiraMediaPlayer diraMediaPlayer = getViewHolderManagerContract().getDiraMediaPlayer();

            if (BuildConfig.DEBUG) bubblePlayer.showDebugLog();

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

        });
    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        CardView bubble = new BubbleMessageView(itemView.getContext());
        //  bubbleContainer = find(BubbleMessageView.BUBBLE_CONTAINER_ID);
        outerContainer.addView(bubble);
        bubblePlayer = find(R.id.bubble_player);
        bubblePlayer.attachDebugIndicator(outerContainer);
        getMessageAdapterContract().attachVideoPlayer(bubblePlayer);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        bubblePlayer.reset();
        Attachment bubbleAttachment = message.getAttachments().get(0);

        if (!AttachmentsStorage.isAttachmentSaving(bubbleAttachment))
            onAttachmentLoaded(bubbleAttachment,
                    AttachmentsStorage.getFileFromAttachment(bubbleAttachment,
                            itemView.getContext(), message.getRoomSecret()), message);

    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        if (!isInitialized) return;
        bubblePlayer.stop();
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
        bubblePlayer.play();
    }
}
