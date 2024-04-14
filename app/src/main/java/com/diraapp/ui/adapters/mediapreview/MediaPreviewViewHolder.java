package com.diraapp.ui.adapters.mediapreview;

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
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MediaPreviewViewHolder extends RecyclerView.ViewHolder {

    private AttachmentMessagePair pair;

    private File file;

    private long duration = 60_000;

    private final ViewHolderActivityContract holderActivityContract;

    private final ConstraintLayout imageContainer;

    private final CardView cardView;

    private final TouchImageView imageView;

    private final DiraVideoPlayer videoPlayer;

    private final TextView messageText, memberName, timeText, sizeView, progressTime;

    private final FrameLayout watchButton;

    private final LinearLayout saveButton;

    private final LinearLayout progressLayout;

    private final SeekBar seekBar;

    private final ImageView pauseButton;

    private boolean isSetup = false;

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
        progressTime = itemView.findViewById(R.id.progress_time);
        seekBar = itemView.findViewById(R.id.seek_bar);
        pauseButton = itemView.findViewById(R.id.pause_button);

        holderActivityContract = contract;

        watchButton.setOnClickListener((View v) -> contract.onWatchClicked());

        videoPlayer.setVolume(1);
        contract.attachVideoPlayer(videoPlayer);

        videoPlayer.addListener((DiraVideoPlayerState state) -> {
            if (state != DiraVideoPlayerState.PLAYING) return false;
            if (isSetup) return false;

            onVideoPlayerPrepared();

            return false;
        });
    }

    public void bind(AttachmentMessagePair attachmentMessagePair) {
        pair = attachmentMessagePair;

        videoPlayer.reset();

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

    public void onSelected() {
        if (pair == null) return;
        if (file == null) return;
        Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(), "Holder selected");
        boolean isImage = pair.getAttachment().getAttachmentType() == AttachmentType.IMAGE;
        if (isImage) return;

        videoPlayer.setProgress(0);
        if (videoPlayer.getState() == DiraVideoPlayerState.PAUSED) {
            //final File currentFile = file;
            //videoPlayer.play(() -> onVideoPlayerPrepared(currentFile));
            videoPlayer.play();

            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));
        }
    }

    public void onUnselected() {
        videoPlayer.pause();
    }

    public void onRecycled() {
        isSetup = false;
        pair = null;
        file = null;

        pauseButton.setOnClickListener((View v) -> {});
        videoPlayer.setOnClickListener((View v) -> {});
        videoPlayer.setOnTickListener((float progress) -> {});

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        duration = 60_000;

        imageView.setImageBitmap(null);
        videoPlayer.stop();
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

            progressLayout.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            duration = DeviceUtils.readDuration(file, imageView.getContext());

//            final File currentFile = file;
//            videoPlayer.play(file.getPath(), () -> onVideoPlayerPrepared(currentFile));
            videoPlayer.play(file.getPath());


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
        DiraVideoPlayerState state = videoPlayer.getState();

        if (state == DiraVideoPlayerState.PLAYING) {
            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_play));
            videoPlayer.pause();

        } else if (state == DiraVideoPlayerState.PAUSED) {
            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));
            videoPlayer.play();
        }
    }

    private void onVideoPlayerPrepared() {
        DiraActivity.runOnMainThread(() -> {
            isSetup = true;
            imageView.setVisibility(View.GONE);

            setPlayButtonListener();

            Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(),
                    "DiraVideoPlayer loaded");

            videoPlayer.setOnTickListener((float progress) -> {
                seekBar.setProgress((int) (progress * 1000));
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                    if (!fromUser) return;

                    videoPlayer.setProgress((float) i / 1000);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            boolean isSelected = pair != null;
            if (isSelected) isSelected = holderActivityContract.checkIsSelected(pair.getAttachment());

            if (!isSelected) {
                Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(),
                        "DiraVideoPlayer loaded, but detached | " + !isSelected);
                onUnselected();
                return;
            }

            videoPlayer.setSpeed(1f);
            videoPlayer.setProgress(0);

        });
    }

    public interface ViewHolderActivityContract {

        void onWatchClicked();

        void attachVideoPlayer(DiraVideoPlayer videoPlayer);

        boolean checkIsSelected(Attachment attachment);
    }
}
