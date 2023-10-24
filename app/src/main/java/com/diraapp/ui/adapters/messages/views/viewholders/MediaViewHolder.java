package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.ui.components.RoomMediaMessage;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;
import com.diraapp.utils.StringFormatter;

import java.io.File;

public class MediaViewHolder extends AttachmentViewHolder {

    private DiraVideoPlayer videoPlayer;
    private ImagePreview previewImage;
    private CardView imageContainer;
    private TextView messageText;

    private Attachment currentAttachment;
    private File currentMediaFile;

    public MediaViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) return;
        if(attachment != currentAttachment) return;
        previewImage.hideDownloadOverlay();

        if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
            previewImage.setVisibility(View.VISIBLE);
            //previewImage.setImage(file);

            previewImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO,
                            previewImage.getLoadedBitmap(), imageContainer).start();
                }
            });

        } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {

            previewImage.setVisibility(View.VISIBLE);

            videoPlayer.setVisibility(View.VISIBLE);
            videoPlayer.attachDebugIndicator(postInflatedViewsContainer);
            DiraVideoPlayer finalVideoPlayer = videoPlayer;
            previewImage.post(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                            (previewImage.getLayoutParams().width,
                                    previewImage.getLayoutParams().height);
                    finalVideoPlayer.setLayoutParams(params);


                    AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                    alphaAnimation.setDuration(500);

                  //  finalVideoPlayer.setVisibility(View.GONE);
                    alphaAnimation.setFillAfter(true);
                    finalVideoPlayer.startAnimation(alphaAnimation);
                }
            });


            videoPlayer.play(file.getPath());


            videoPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO,
                            previewImage.getLoadedBitmap(), imageContainer).start();
                }
            });
        }
    }

    @Override
    public void onLoadFailed() {

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new RoomMediaMessage(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);
        previewImage = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        imageContainer = itemView.findViewById(R.id.image_container);
        messageText = itemView.findViewById(R.id.message_text);
        previewImage.setVisibility(View.VISIBLE);
        getMessageAdapterContract().attachVideoPlayer(videoPlayer);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        videoPlayer.reset();
        videoPlayer.setVisibility(View.GONE);
        previewImage.setVisibility(View.VISIBLE);


        String text = message.getText();
        if ((text != null) && (!StringFormatter.EMPTY_STRING.equals(text))) {
            messageText.setText(text);
            messageText.setVisibility(View.VISIBLE);
        } else {
            messageText.setVisibility(View.GONE);
        }

        Attachment attachment = message.getAttachments().get(0);
        currentAttachment = attachment;
        currentMediaFile = AttachmentsStorage.getFileFromAttachment(attachment,
                itemView.getContext(), message.getRoomSecret());
        previewImage.setAttachment(attachment, getMessageAdapterContract().getRoom(), currentMediaFile, () -> {
            if (currentAttachment != attachment) return;
            if (!AttachmentsStorage.isAttachmentSaving(attachment))
                onAttachmentLoaded(attachment, currentMediaFile, message);
        });


    }


    public void onViewRecycled() {
        super.onViewRecycled();
        if (!isInitialized) return;
        videoPlayer.stop();
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (!isInitialized) return;
        videoPlayer.pause();
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        if (!isInitialized) return;
        if (videoPlayer.getState() == DiraVideoPlayerState.PAUSED && currentMediaFile != null)
            videoPlayer.play(currentMediaFile.getPath());
    }


}
