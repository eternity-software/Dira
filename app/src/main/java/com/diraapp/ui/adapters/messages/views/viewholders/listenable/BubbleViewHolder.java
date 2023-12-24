package com.diraapp.ui.adapters.messages.views.viewholders.listenable;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.AttachmentListenedRequest;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnablePerformRequestException;
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

        setState(ListenableViewHolderState.UNSELECTED);
        if (getCurrentMessage() == null) return;
        onAttachmentLoaded(
                getCurrentMessage().getSingleAttachment(), currentMediaFile, getCurrentMessage());
    }

    @Override
    public void setProgress(float progress) {
        if (!isInitialized) return;

        //bubblePlayer.setProgress(progress);
    }

    @Override
    public void pause(boolean isPaused, float progress) {
        if (!isInitialized) return;

        if (isPaused) {
            bubblePlayer.pause();
            bubblePlayer.setProgress(progress / 10);
            setState(ListenableViewHolderState.PAUSED);
        } else {

            bubblePlayer.play(() -> new Handler(Looper.getMainLooper()).post(() -> {
                bubblePlayer.setSpeed(1f);
                bubblePlayer.setProgress(progress / 10);
            }));

            setState(ListenableViewHolderState.PLAYING);
        }
    }

    @Override
    public void start() {
        if (!isInitialized) return;

        bubblePlayer.setSpeed(1f);
        bubblePlayer.setProgress(GlobalMediaPlayer.getInstance().getCurrentProgress() / 10);
        setState(ListenableViewHolderState.PLAYING);
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

        bubblePlayer.setOnClickListener((View v) -> {

            sendMessageListened(message);

            //if (BuildConfig.DEBUG) bubblePlayer.showDebugLog();

            if (getState() == ListenableViewHolderState.PAUSED ||
                    getState() == ListenableViewHolderState.PLAYING) {
                getMessageAdapterContract().currentListenablePaused(this);
            }  else {
                getMessageAdapterContract().currentListenableStarted(this, file, 0);
            }
        });

//        bubblePlayer.setOnClickListener(v -> {
//            sendMessageListened(message);
//
//            DiraMediaPlayer diraMediaPlayer = getViewHolderManagerContract().getDiraMediaPlayer();
//
//            if (BuildConfig.DEBUG) bubblePlayer.showDebugLog();
//
//            try {
//                if (diraMediaPlayer.isPlaying()) {
//                    diraMediaPlayer.stop();
//                }
//                diraMediaPlayer.reset();
//                diraMediaPlayer.setDataSource(file.getPath());
//
//                diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//
//                        diraMediaPlayer.start();
//                        //timeText.setText(AppStorage.getStringSize(attachment.getSize()));
//                        ((DiraVideoPlayer) v).setSpeed(1f);
//                        ((DiraVideoPlayer) v).setProgress(0);
//                        diraMediaPlayer.setOnPreparedListener(null);
//
//                    }
//                });
//                diraMediaPlayer.prepareAsync();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        });
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
        bubblePlayer.reset();

        updateListeningIndicator(message.getSingleAttachment());

        Attachment bubbleAttachment = message.getAttachments().get(0);
        currentMediaFile = AttachmentDownloader.getFileFromAttachment(bubbleAttachment,
                itemView.getContext(), message.getRoomSecret());

        if (!AttachmentDownloader.isAttachmentSaving(bubbleAttachment))
            onAttachmentLoaded(bubbleAttachment,
                    currentMediaFile, message);

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
        bubblePlayer.play(currentMediaFile.getPath());
    }

    private void sendMessageListened(Message message) {
        if (!message.hasAuthor()) return;
        if (isSelfMessage) return;

        Attachment attachment = message.getSingleAttachment();
        if (attachment.isListened()) return;

        AttachmentListenedRequest request =
                new AttachmentListenedRequest(message.getRoomSecret(), message.getId(), getSelfId());

        try {
            UpdateProcessor.getInstance().
                    sendRequest(request, getMessageAdapterContract().getRoom().getServerAddress());
            attachment.setListened(true);
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
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
}
