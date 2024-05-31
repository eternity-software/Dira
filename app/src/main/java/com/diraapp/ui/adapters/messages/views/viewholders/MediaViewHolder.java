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
import com.diraapp.storage.AttachmentDownloadHandler;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivityListener;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.ui.components.RoomMediaMessage;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.utils.StringFormatter;

import java.io.File;

public class MediaViewHolder extends AttachmentViewHolder implements DiraActivityListener, AttachmentDownloadHandler {

    private DiraVideoPlayer videoPlayer;
    private ImagePreview previewImage;
    private CardView imageContainer;
    private TextView messageText;

    private Attachment currentAttachment;
    private File currentMediaFile;

    private boolean isAttachmentLoaded = false;
    private boolean isBind = false;

    public MediaViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);
        messageAdapterContract.addListener(this);
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) return;
        if (isAttachmentLoaded) return;
        if (attachment != currentAttachment) return;
        previewImage.hideOverlay();
        currentAttachment = attachment;

        if (!previewImage.isMainImageLoaded())
            previewImage.loadAttachmentFile(file);

        previewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaViewHolder.this.openMediaPreviewActivity(file.getPath(),
                        attachment.getAttachmentType() == AttachmentType.VIDEO,
                        previewImage.getLoadedBitmap(), previewImage.getImageView(), attachment);
            }
        });

        if (attachment.getAttachmentType() == AttachmentType.IMAGE) {

            isAttachmentLoaded = true;

        } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {


            isAttachmentLoaded = true;
            videoPlayer.setVisibility(View.VISIBLE);
            videoPlayer.attachDebugIndicator(postInflatedViewsContainer);
            DiraVideoPlayer finalVideoPlayer = videoPlayer;
            previewImage.post(() -> {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                        (previewImage.getLayoutParams().width,
                                previewImage.getLayoutParams().height);
                finalVideoPlayer.setLayoutParams(params);


                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                alphaAnimation.setDuration(500);

                //  finalVideoPlayer.setVisibility(View.GONE);
                alphaAnimation.setFillAfter(true);
                finalVideoPlayer.startAnimation(alphaAnimation);
            });

            videoPlayer.play(file.getPath());

            videoPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMediaPreviewActivity(file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO,
                            previewImage.getLoadedBitmap(),
                            previewImage.getImageView(), attachment);
                }
            });
        }
    }

    @Override
    public void onLoadFailed(Attachment attachment) {
        previewImage.displayTrash();
    }

    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new RoomMediaMessage(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);
        previewImage = view.findViewById(R.id.image_view);
        videoPlayer = view.findViewById(R.id.video_player);
        imageContainer = view.findViewById(R.id.image_container);
        messageText = itemView.findViewById(R.id.message_text);
        previewImage.setVisibility(View.VISIBLE);
        previewImage.setWaterfallBalancer(getMessageAdapterContract().getWaterfallBalancer());
        getMessageAdapterContract().addListener(previewImage);
        getMessageAdapterContract().attachVideoPlayer(videoPlayer);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        previewImage.hideOverlay();


        isAttachmentLoaded = false;
        videoPlayer.reset();
        videoPlayer.setVisibility(View.GONE);
        previewImage.setVisibility(View.VISIBLE);
        videoPlayer.setOnClickListener(v -> {
        });
        previewImage.setOnClickListener(v -> {
        });

        Attachment attachment = message.getSingleAttachment();
        currentMediaFile = AttachmentDownloader.getFileFromAttachment(attachment,
                itemView.getContext(), message.getRoomSecret());


        currentAttachment = attachment;
        previewImage.setAttached(true);
        previewImage.prepareForAttachment(attachment, getMessageAdapterContract().getRoom(), () -> {
            if (currentAttachment != attachment) {
                return;
            }

            // Load an existing attachment
            if (!AttachmentDownloader.isAttachmentSaving(attachment))
                onAttachmentLoaded(attachment, currentMediaFile, message);
        });


        String text = message.getText();
        if ((text != null) && (!StringFormatter.EMPTY_STRING.equals(text))) {
            messageText.setText(text);
            messageText.setVisibility(View.VISIBLE);
        } else {
            messageText.setVisibility(View.GONE);
        }

        if (currentMediaFile == null | AttachmentDownloader.isAttachmentSaving(attachment)) {
            AttachmentDownloader.setDownloadHandlerForAttachment(this, attachment);
        }

        previewImage.showOverlay(currentMediaFile, attachment);


        previewImage.loadAttachmentFile(currentMediaFile);
        isBind = true;
    }

    public void onLoadPercentChanged(Attachment attachment, int percent) {
        if (attachment == currentAttachment) {
            previewImage.setDownloadPercent(percent);
        }
    }

    public void onViewRecycled() {
        super.onViewRecycled();
        if (!isInitialized) return;
        videoPlayer.stop();
        previewImage.detach();
        isBind = false;
    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        if (!isInitialized) return;
        videoPlayer.pause();
        previewImage.detach();
        isBind = false;
    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        if (!isInitialized) return;
        previewImage.attach();
        if (currentMediaFile != null) {
            videoPlayer.play(currentMediaFile.getPath());
        }
    }

    @Override
    public void onDestroy() {
        AttachmentDownloader.removeAttachmentDownloadHandler(this, currentAttachment);
    }

    @Override
    public void onProgressChanged(int progress) {
        onLoadPercentChanged(currentAttachment, progress);
    }
}
