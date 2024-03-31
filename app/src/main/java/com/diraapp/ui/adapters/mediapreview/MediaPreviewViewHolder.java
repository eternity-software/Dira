package com.diraapp.ui.adapters.mediapreview;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MediaPreviewViewHolder extends RecyclerView.ViewHolder {

    private AttachmentMessagePair pair;

    private File file;

    private boolean isMediaShown = false;

    private int boundCount = 0;

    private final ViewHolderActivityContract holderActivityContract;

    private final ConstraintLayout imageContainer;

    private final CardView cardView;

    private final TouchImageView imageView;

    private final DiraVideoPlayer videoPlayer;

    private final TextView messageText, memberName, timeText, sizeView, timeView;

    private final FrameLayout watchButton;

    private final LinearLayout saveButton;

    private final LinearLayout progressLayout;

    private final SeekBar seekBar;

    private final ImageView pauseButton;

    public MediaPreviewViewHolder(@NonNull View itemView, ViewHolderActivityContract contract) {
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

        holderActivityContract = contract;

        watchButton.setOnClickListener((View v) -> contract.onWatchClicked());

        videoPlayer.setVolume(1);
        videoPlayer.setOnTickListener((float progress) -> {
            seekBar.setProgress((int) (progress * 1000f));
        });

        contract.attachVideoPlayer(videoPlayer);
    }

    public void bind(AttachmentMessagePair attachmentMessagePair) {
        isMediaShown = true;
        pair = attachmentMessagePair;

        boundCount++;
        Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(), "Bound, times = " + boundCount);

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

        file = AttachmentDownloader.getFileFromAttachment(pair.getAttachment(),
                itemView.getContext(), pair.getMessage().getRoomSecret());

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

        imageView.setImageBitmap(null);
        videoPlayer.pause();
    }

    public void release() {
        isMediaShown = false;
        pair = null;
        file = null;

        imageView.setImageBitmap(null);
        videoPlayer.stop();
        videoPlayer.reset();
    }

    private void showContent() {
        boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;

        if (file == null) {
            videoPlayer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            progressLayout.setVisibility(View.GONE);
            return;
        }

        if (isVideo) {
            imageView.setVisibility(View.VISIBLE);
            videoPlayer.setVisibility(View.VISIBLE);
            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));

            pauseButton.setOnClickListener(null);
            videoPlayer.setOnClickListener(null);

            progressLayout.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            videoPlayer.play(file.getPath(), () -> DiraActivity.runOnMainThread(() -> {
                if (!isMediaShown) {
                    onDetached();
                    return;
                }

                videoPlayer.setSpeed(1f);
                videoPlayer.setProgress(0);
                imageView.setVisibility(View.GONE);

                setPlayButtonListener();

                Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(), "DiraVideoPlayer loaded");
            }));


        } else {
            videoPlayer.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);

            Picasso.get().load(file).placeholder(R.drawable.full_placeholder).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void setPlayButtonListener() {
        pauseButton.setOnClickListener((View v) -> {
            onPauseClicked();
        });

        videoPlayer.setOnClickListener((View v) -> {
            onPauseClicked();
        });
    }

    private void onPauseClicked() {
        if (file == null) return;
        if (!isMediaShown) return;
        boolean isPlaying = videoPlayer.isPlaying();

        if (isPlaying) {
            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_play));
            videoPlayer.pause();
            return;
        }

        pauseButton.setImageDrawable(AppCompatResources.
                getDrawable(itemView.getContext(), R.drawable.ic_pause));
        videoPlayer.play();
    }

    public interface ViewHolderActivityContract {

        void onWatchClicked();

        void attachVideoPlayer(DiraVideoPlayer videoPlayer);
    }
}
