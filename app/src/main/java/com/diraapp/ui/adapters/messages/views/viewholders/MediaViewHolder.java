package com.diraapp.ui.adapters.messages.views.viewholders;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.RoomMessageVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;
import com.diraapp.utils.Logger;
import com.diraapp.utils.StringFormatter;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaViewHolder extends AttachmentViewHolder {

    private DiraVideoPlayer videoPlayer;
    private ImageView imageView;
    private CardView imageContainer;
    private TextView messageText;

    private Attachment currentAttachment;
    private File currentPlayingFile;

    public MediaViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if(file == null) return;

        if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
            imageView.setVisibility(View.VISIBLE);
            DiraActivity.runGlobalBackground(() -> {
                Bitmap bitmap = AppStorage.getBitmapFromPath(file.getPath(), this.itemView.getContext());
                new Handler(Looper.getMainLooper()).post(() -> {
                    imageView.setImageBitmap(bitmap);
                });
            });

            // Causing "blink"
            // Picasso.get().load(Uri.fromFile(file)).into(holder.imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO,
                            attachment.getBitmapPreview(), imageContainer).start();
                }
            });

        } else if (attachment.getAttachmentType() == AttachmentType.VIDEO) {

            imageView.setVisibility(View.VISIBLE);

            videoPlayer.setVisibility(View.VISIBLE);
            videoPlayer.attachDebugIndicator(postInflatedViewsContainer);
            DiraVideoPlayer finalVideoPlayer = videoPlayer;
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                            (imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
                    finalVideoPlayer.setLayoutParams(params);


                    AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
                    alphaAnimation.setDuration(500);

                    alphaAnimation.setFillAfter(true);
                    finalVideoPlayer.startAnimation(alphaAnimation);
                }
            });


            videoPlayer.play(file.getPath());


            try {
                //loading.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            videoPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMessageAdapterContract().preparePreviewActivity(file.getPath(),
                            attachment.getAttachmentType() == AttachmentType.VIDEO,
                            attachment.getBitmapPreview(), imageContainer).start();
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
        View view = new RoomMessageVideoPlayer(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        imageContainer = itemView.findViewById(R.id.image_container);
        messageText = itemView.findViewById(R.id.message_text);
        imageView.setVisibility(View.VISIBLE);
        getMessageAdapterContract().attachVideoPlayer(videoPlayer);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        videoPlayer.reset();
        videoPlayer.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);


        String text = message.getText();
        if ((text != null) && (!StringFormatter.EMPTY_STRING.equals(text))) {
            messageText.setText(text);
            messageText.setVisibility(View.VISIBLE);
        } else {
            messageText.setVisibility(View.GONE);
        }

        Attachment attachment = message.getAttachments().get(0);
        currentAttachment = attachment;
        DiraActivity.runGlobalBackground(() -> {
            Bitmap previewBitmap = attachment.getBitmapPreview();
            if (previewBitmap == null) {
                previewBitmap = Bitmap.createBitmap(attachment.getWidth(),
                        attachment.getHeight(),
                        Bitmap.Config.ARGB_8888);

            }
            Bitmap finalPreviewBitmap = previewBitmap;
            new Handler(Looper.getMainLooper()).post(() -> {
                if(currentAttachment != attachment) return;
                imageView.setImageBitmap(finalPreviewBitmap);
                currentPlayingFile = AttachmentsStorage.getFileFromAttachment(attachment,
                        itemView.getContext(), message.getRoomSecret());
                if (!AttachmentsStorage.isAttachmentSaving(attachment))
                    onAttachmentLoaded(attachment,currentPlayingFile, message);
            });
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
        if (videoPlayer.getState() == DiraVideoPlayerState.PAUSED && currentPlayingFile != null)
            videoPlayer.play(currentPlayingFile.getPath());
    }


}
