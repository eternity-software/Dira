package com.diraapp.ui.adapters.messages.views.viewholders.listenable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.utils.Logger;

import java.io.File;

public class BubbleViewHolder extends ListenableViewHolder {

    private DiraVideoPlayer bubblePlayer;
    private BubbleMessageView bubbleContainer;

    private float lastProgress = 0;

    private File currentMediaFile;

    public BubbleViewHolder(@NonNull ViewGroup itemView,
                            MessageAdapterContract messageAdapterContract,
                            ViewHolderManagerContract viewHolderManagerContract,
                            boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

        setOuterContainer(true);
    }

    @Override
    public void clearProgress() {
        if (!isInitialized) return;

        clearItem();
        if (getCurrentMessage() == null | currentMediaFile == null) {
            return;
        }

        bubblePlayer.play(currentMediaFile.getPath());
        bubblePlayer.setProgress(0);
    }

    @Override
    public void setProgress(float progress) {
        if (!isInitialized) {
        }

        //bubblePlayer.setProgress(progress);
    }

    @Override
    public void pause(boolean isPaused, float progress) {
        if (!isInitialized) return;

        if (isPaused) {
            setState(ListenableViewHolderState.PAUSED);
            bubblePlayer.pause();
            bubblePlayer.setProgress(progress / 10);
        } else {
            setState(ListenableViewHolderState.PLAYING);
            lastProgress = progress / 10;
            bubblePlayer.play();

        }
    }

    @Override
    public void start() {
        if (!isInitialized) return;

        lastProgress = 0;
        bubblePlayer.setSpeed(1f);
        bubblePlayer.setProgress(GlobalMediaPlayer.getInstance().getCurrentProgress() / 10);
        setState(ListenableViewHolderState.PLAYING);
    }

    @Override
    public void rebindPlaying(boolean isPaused, float progress) {
        if (!isInitialized) return;

        if (isPaused) {
            setState(ListenableViewHolderState.PAUSED);
        } else {
            setState(ListenableViewHolderState.PLAYING);
        }
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) return;

        currentMediaFile = file;
        setCurrentMessage(message);

        checkIsCurrent();

        bubblePlayer.setOnClickListener((View v) -> {

            sendMessageListened(message);

            //if (BuildConfig.DEBUG) bubblePlayer.showDebugLog();

            if (getState() == ListenableViewHolderState.PAUSED ||
                    getState() == ListenableViewHolderState.PLAYING) {
                getMessageAdapterContract().currentListenablePaused(this);
            } else {
                getMessageAdapterContract().currentListenableStarted(this, file, 0);
            }
        });
    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        CardView bubble = new BubbleMessageView(itemView.getContext(), isSelfMessage);
        //  bubbleContainer = find(BubbleMessageView.BUBBLE_CONTAINER_ID);
        outerContainer.addView(bubble);
        bubblePlayer = find(R.id.bubble_player);
        bubblePlayer.attachDebugIndicator(outerContainer);
        getMessageAdapterContract().attachVideoPlayer(bubblePlayer);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        if (message.getAttachments().size() == 0) return;

        updateListeningIndicator(message.getSingleAttachment());

        Attachment bubbleAttachment = message.getAttachments().get(0);
        currentMediaFile = AttachmentDownloader.getFileFromAttachment(bubbleAttachment,
                itemView.getContext(), message.getRoomSecret());

        if (!AttachmentDownloader.isAttachmentSaving(bubbleAttachment))
            onAttachmentLoaded(bubbleAttachment, currentMediaFile, message);
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
        if (!isInitialized | currentMediaFile == null) return;
        checkIsCurrent();
    }

    @Override
    public void updateListeningIndicator(Attachment attachment) {
        if (!isInitialized) return;

        LinearLayout indicator = itemView.findViewById(R.id.listened_indicator);
        if (!attachment.isListened()) {
            indicator.setVisibility(View.VISIBLE);
        } else {
            indicator.setVisibility(View.INVISIBLE);
        }
    }

    private void clearItem() {
        setState(ListenableViewHolderState.UNSELECTED);
        bubblePlayer.reset();
    }

    private void checkIsCurrent() {
        if (!isInitialized | currentMediaFile == null) return;

        switch (getState()) {
            case UNSELECTED:
                clearProgress();
                break;
            case PLAYING:
                bubblePlayer.play(currentMediaFile.getPath(), () -> {
                    bubblePlayer.setSpeed(1f);
                    bubblePlayer.setProgress(GlobalMediaPlayer.getInstance().getCurrentProgress() / 10);
                });
                break;
            case PAUSED:
                bubblePlayer.play(currentMediaFile.getPath(), () -> {
                    bubblePlayer.setSpeed(1f);
                    bubblePlayer.pause();
                    bubblePlayer.setProgress(GlobalMediaPlayer.getInstance().getCurrentProgress() / 10);
                });
                break;
        }

        Logger.logDebug(this.getClass().getName(), "Rebind with " + getState() +
                " | " + getCurrentMessage().getId());
    }
}
