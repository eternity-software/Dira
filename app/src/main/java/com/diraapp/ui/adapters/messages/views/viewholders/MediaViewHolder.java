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

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaViewHolder extends AttachmentViewHolder {

    private DiraVideoPlayer videoPlayer;
    private ImageView imageView;
    private CardView imageContainer;
    private TextView messageText;

    public MediaViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if(file == null) return;
        AtomicBoolean isMainImageLoaded = new AtomicBoolean(false);
        DiraActivity.runGlobalBackground(() -> {

            Bitmap previewBitmap = attachment.getBitmapPreview();
            if (previewBitmap == null) {


            } else {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(!isMainImageLoaded.get()) imageView.setImageBitmap(previewBitmap);
                });

            }
        });
        if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
            imageView.setVisibility(View.VISIBLE);
            DiraActivity.runGlobalBackground(() -> {
                Bitmap bitmap = AppStorage.getBitmapFromPath(file.getPath(), this.itemView.getContext());
                new Handler(Looper.getMainLooper()).post(() -> {
                    isMainImageLoaded.set(true);
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

            //videoPlayer.attachDebugIndicator(viewsContainer);
            videoPlayer.setVisibility(View.VISIBLE);
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

            getMessageAdapterContract().attachVideoPlayer(videoPlayer);
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
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        videoPlayer.reset();
        imageView.setVisibility(View.GONE);
        videoPlayer.setVisibility(View.GONE);



        Attachment attachment = message.getAttachments().get(0);
        Bitmap bmp = Bitmap.createBitmap(attachment.getWidth(),
                attachment.getHeight(),
                Bitmap.Config.ARGB_8888);

        imageView.setImageBitmap(bmp);


        if (!AttachmentsStorage.isAttachmentSaving(attachment))
            onAttachmentLoaded(attachment, AttachmentsStorage.getFileFromAttachment(attachment,
                    itemView.getContext(), message.getRoomSecret()), message);

    }


}
