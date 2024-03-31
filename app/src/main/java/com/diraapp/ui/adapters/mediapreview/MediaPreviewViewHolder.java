package com.diraapp.ui.adapters.mediapreview;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.VideoPlayerException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MediaPreviewViewHolder extends RecyclerView.ViewHolder {

    private AttachmentMessagePair pair;

    private boolean isMediaShown = false;

    private final ConstraintLayout imageContainer;

    private final CardView cardView;

    private final TouchImageView imageView;

    private final VideoPlayer videoPlayer;

    private final TextView messageText, memberName, timeText, sizeView, timeView;

    private final FrameLayout watchButton;

    private final LinearLayout saveButton;

    private final LinearLayout progressLayout;

    private final SeekBar seekBar;

    private final ImageView pauseButton;

    private boolean isVideoPlayerReady = false;

    public MediaPreviewViewHolder(@NonNull View itemView, WatchCallBack watchCallBack) {
        super(itemView);

        imageContainer = itemView.findViewById(R.id.imageContainer);
        cardView = itemView.findViewById(R.id.card_view);
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        messageText = itemView.findViewById(R.id.message_text);
        memberName = itemView.findViewById(R.id.member_name);
        timeText = itemView.findViewById(R.id.time);
        watchButton = itemView.findViewById(R.id.watch);
        saveButton = itemView.findViewById(R.id.save_button);
        sizeView = itemView.findViewById(R.id.size_view);

        progressLayout = itemView.findViewById(R.id.progress_layout);
        timeView = itemView.findViewById(R.id.time_view);
        seekBar = itemView.findViewById(R.id.seek_bar);
        pauseButton = itemView.findViewById(R.id.pause_button);

        watchButton.setOnClickListener((View v) -> watchCallBack.onWatchClicked());
    }

    public void bind(AttachmentMessagePair attachmentMessagePair) {
        isMediaShown = true;
        pair = attachmentMessagePair;

        Message message = pair.getMessage();
        String text = message.getText();
        if (text == null) {
            messageText.setVisibility(View.GONE);
        } else if (text.length() == 0) {
            messageText.setVisibility(View.GONE);
        } else {
            messageText.setText(message.getText());
        }

        memberName.setText(message.getAuthorNickname());
        timeText.setText(DeviceUtils.getDateFromTimestamp(message.getTime(), false));
        sizeView.setText(AppStorage.getStringSize(pair.getAttachment().getSize()));

        showContent();
    }

    public void onAttached() {
        if (isMediaShown) return;
        if (pair == null) return;

        isMediaShown = true;
        showContent();
    }

    public void onDetached() {
        isMediaShown = false;
        releaseMedia();
    }

    public void release() {
        isMediaShown = false;
        pair = null;

        releaseMedia();
    }

    private void showContent() {
        boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;

        File file = AttachmentDownloader.getFileFromAttachment(pair.getAttachment(),
                itemView.getContext(), pair.getMessage().getRoomSecret());

        if (file == null) {
            videoPlayer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            progressLayout.setVisibility(View.GONE);
            return;
        }

        if (isVideo) {
            videoPlayer.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            progressLayout.setVisibility(View.VISIBLE);
            setupVideo(file.getAbsolutePath());


        } else {
            videoPlayer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            progressLayout.setVisibility(View.GONE);

            Picasso.get().load(file).placeholder(R.drawable.full_placeholder).into(imageView);
        }
    }

    private void setupVideo(String path) {

        if (isVideoPlayerReady) {
            try {
                videoPlayer.play(path);
                videoPlayer.setVolume(1);
            } catch (VideoPlayerException e) {
                e.printStackTrace();
            }

            return;
        }

        videoPlayer.setVideoPlayerListener(new VideoPlayer.VideoPlayerListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onReleased() {

            }

            @Override
            public void onReady(int width, int height) {
                try {
                    videoPlayer.play(path);
                    videoPlayer.setVolume(1);

                    isVideoPlayerReady = true;
                } catch (VideoPlayerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void releaseMedia() {
        imageView.setImageBitmap(null);
        videoPlayer.release();
    }

    public interface WatchCallBack {

        void onWatchClicked();
    }
}
