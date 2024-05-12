package com.diraapp.ui.adapters.mediapreview;

import android.animation.Animator;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayerState;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MediaPreviewViewHolder extends RecyclerView.ViewHolder {

    private AttachmentMessagePair pair;

    private File file;

    private boolean isSaved = false;

    private long duration = 60_000;

    private long currentTime = -1000L;

    private final ViewHolderActivityContract holderActivityContract;

    private final LinearLayout messageInfoContainer;

    private final CardView cardView;

    private final PhotoView imageView;

    private final DiraVideoPlayer videoPlayer;

    private final TextView messageText, memberName, timeText, sizeView, progressTime;

    private final FrameLayout watchButton;

    private final LinearLayout saveButton;

    private final LinearLayout progressLayout;

    private final SeekBar seekBar;

    private final ImageView pauseButton, saveButtonIcon, memberImage, pauseButtonMiddle;

    private boolean isSetup = false;

    private boolean isPaused = false;

    private boolean isInterfaceShown = true;


    public MediaPreviewViewHolder(@NonNull View itemView, ViewHolderActivityContract contract) {
        super(itemView);

        messageInfoContainer = itemView.findViewById(R.id.bottom_layout);
        pauseButtonMiddle = itemView.findViewById(R.id.pause_button_middle);

        //imageContainer = itemView.findViewById(R.id.imageContainer);
        cardView = itemView.findViewById(R.id.card_view);
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);

        messageText = itemView.findViewById(R.id.message_text);
        memberName = itemView.findViewById(R.id.member_name);
        memberImage = itemView.findViewById(R.id.member_picture);
        timeText = itemView.findViewById(R.id.time);
        watchButton = itemView.findViewById(R.id.watch);

        saveButton = itemView.findViewById(R.id.save_button);
        saveButtonIcon = itemView.findViewById(R.id.save_button_icon);
        sizeView = itemView.findViewById(R.id.size_view);

        progressLayout = itemView.findViewById(R.id.progress_layout);
        progressTime = itemView.findViewById(R.id.progress_time);
        seekBar = itemView.findViewById(R.id.seek_bar);
        pauseButton = itemView.findViewById(R.id.pause_button);

        holderActivityContract = contract;

        watchButton.setOnClickListener((View v) -> {
            if (pair == null) return;

            String messageId = pair.getMessage().getId();
            long messageTime = pair.getMessage().getTime();
            contract.onWatchClicked(messageId, messageTime);
        });

        videoPlayer.setVolume(1);
        videoPlayer.setPlayOnResume(false);
        contract.attachVideoPlayer(videoPlayer);

        videoPlayer.addListener((DiraVideoPlayerState state) -> {
            if (state == DiraVideoPlayerState.PAUSED) {
                pauseButton.setImageDrawable(AppCompatResources.
                        getDrawable(itemView.getContext(), R.drawable.ic_play));

                pauseButtonMiddle.setImageDrawable(AppCompatResources.
                        getDrawable(itemView.getContext(), R.drawable.ic_play));
            }

            if (state != DiraVideoPlayerState.PLAYING) return false;

            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));

            pauseButtonMiddle.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));

            if (pair == null) {
                onUnselected();
                return false;
            }

            boolean isImage = pair.getAttachment().getAttachmentType() == AttachmentType.IMAGE;
            if (isImage) return false;


            DiraActivity.runOnMainThread(() -> {
                if (!isSetup) {
                    onVideoPlayerPrepared();
                }

                setupVideoPlayerListener();

                videoPlayer.setSpeed(1f);

//                if (isPaused) videoPlayer.setProgress(0);

                boolean isSelected = holderActivityContract.checkIsSelected(pair);
                if (!isSelected) {
                    Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(),
                            "DiraVideoPlayer loaded, but detached | " + !isSelected);
                    onUnselected();
                }
            });

            return false;
        });

        setPlayButtonListener();

        saveButton.setOnClickListener((View v) -> {
            if (isSaved) return;
            if (file == null) return;
            if (pair == null) return;

            isSaved = true;

            boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;
            contract.saveAttachment(file.getAbsolutePath(), isVideo);

            saveButtonIcon.setImageDrawable(ContextCompat.getDrawable(
                    itemView.getContext(), R.drawable.ic_check));
        });

        setupContentOnClickListener();
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

        bindMember(message);

        timeText.setText(
                DeviceUtils.getDateFromTimestamp(message.getTime(), false) + ", " +
                        DeviceUtils.getTimeFromTimestamp(message.getTime()));
        sizeView.setText(AppStorage.getStringSize(pair.getAttachment().getSize()));

        file = AttachmentDownloader.getFileFromAttachment(pair.getAttachment(),
                itemView.getContext(), pair.getMessage().getRoomSecret());

        showContent();
    }

    private void bindMember(Message message) {
        Member member = holderActivityContract.getMember(message.getAuthorId());

        if (member != null) {
            memberName.setText(member.getNickname());

            if (member.getImagePath() != null) {
                int imageSize = DeviceUtils.dpToPx(36, itemView.getContext());
                Picasso.get().load(new File(member.getImagePath()))
                        .resize(imageSize, imageSize)
                        .into(memberImage);
            } else {
                memberImage.setImageDrawable(ContextCompat.
                        getDrawable(itemView.getContext(), R.drawable.placeholder));
            }
        } else {
            memberName.setText(message.getAuthorNickname());
            memberImage.setImageDrawable(ContextCompat.
                    getDrawable(itemView.getContext(), R.drawable.placeholder));

        }

    }

    public void onSelected() {
        if (pair == null) return;
        if (file == null) return;
        Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(), "Holder selected");
        boolean isImage = pair.getAttachment().getAttachmentType() == AttachmentType.IMAGE;
        if (isImage) {
            imageView.setZoomable(true);
            return;
        }

        isPaused = false;

        imageView.setZoomable(false);

        videoPlayer.setProgress(0);
        if (videoPlayer.getState() == DiraVideoPlayerState.PAUSED) {
            videoPlayer.play();

            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));
        }
    }

    public void onUnselected() {
        videoPlayer.pause();

        imageView.setScale(1, true);

        if (!isInterfaceShown) onInterfaceViewsBehaviorChanged();
    }

    public void onRecycled() {
        isSetup = false;
        pair = null;
        file = null;

        currentTime = -1000;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        duration = 60_000;

        imageView.setImageBitmap(null);
        videoPlayer.stop();

        if (isSaved) {
            isSaved = false;

            saveButtonIcon.setImageDrawable(ContextCompat.getDrawable(
                    itemView.getContext(), R.drawable.ic_download));
        }
    }

    private void showContent() {
        boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;

        if (file == null) {
            videoPlayer.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            pauseButtonMiddle.setVisibility(View.GONE);

            progressLayout.setVisibility(View.GONE);
            return;
        }

        if (isVideo) {
            imageView.setVisibility(View.VISIBLE);
            videoPlayer.setVisibility(View.VISIBLE);

            pauseButtonMiddle.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));

            pauseButton.setImageDrawable(AppCompatResources.
                    getDrawable(itemView.getContext(), R.drawable.ic_pause));

            progressLayout.setVisibility(View.VISIBLE);
            Picasso.get().load(R.drawable.full_placeholder);

            DiraActivity.runGlobalBackground(() -> {
                duration = DeviceUtils.readDuration(file, itemView.getContext());
            });

            int width = holderActivityContract.getActivityWidth();
            double ratio = (double) pair.getAttachment().getHeight() /
                    (double) pair.getAttachment().getWidth();
            int height = (int) ((double) width * (double) ratio);
            Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(),
                    "height = " + height + " | wight = " + width);

            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) videoPlayer.getLayoutParams();
            params.height = height;
            params.width = width;

            videoPlayer.play(file.getPath());

            pauseButtonMiddle.setVisibility(View.VISIBLE);

        } else {
            videoPlayer.setVisibility(View.GONE);
            progressLayout.setVisibility(View.GONE);

            pauseButtonMiddle.setVisibility(View.GONE);

            Picasso.get().load(file).placeholder(R.drawable.full_placeholder).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    private void setPlayButtonListener() {
        pauseButton.setOnClickListener((View v) -> {
            onPauseClicked();
        });

        pauseButtonMiddle.setOnClickListener((View v) -> {
            onPauseClicked();
        });
    }

    private void onPauseClicked() {
        if (file == null) return;
        DiraVideoPlayerState state = videoPlayer.getState();

        if (state == DiraVideoPlayerState.PLAYING) {
            videoPlayer.pause();
            isPaused = true;

        } else if (state == DiraVideoPlayerState.PAUSED) {

            videoPlayer.play();
            isPaused = false;

        }
    }

    private void setupContentOnClickListener() {
        imageView.setOnClickListener((View v) -> {
            onInterfaceViewsBehaviorChanged();
        });
        videoPlayer.setOnClickListener((View v) -> {
            onInterfaceViewsBehaviorChanged();
        });

        itemView.setOnClickListener((View v) -> {
            onInterfaceViewsBehaviorChanged();
        });
    }

    private void onInterfaceViewsBehaviorChanged() {
        boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;

        float to;
        if (isInterfaceShown) {
            to = 0;
            isInterfaceShown = false;
        } else {
            to = 1;

            messageInfoContainer.setVisibility(View.VISIBLE);

            if (isVideo) pauseButtonMiddle.setVisibility(View.VISIBLE);

            isInterfaceShown = true;
        }

        performHideAnimation(to, messageInfoContainer);
        if (isVideo) performHideAnimation(to, pauseButtonMiddle);
    }

    private void performHideAnimation(float alphaTo, View v) {
        long animationDuration = 300;
        v.animate().alpha(alphaTo).setDuration(animationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if (!isInterfaceShown) {
                    v.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }
        });
    }

    private void setupVideoPlayerListener() {
        videoPlayer.setOnTickListener((float progress) -> {
            int time = (int) (progress * 1000);
            seekBar.setProgress(time);

            DiraActivity.runOnMainThread(() -> {
                long currentSecond = (long) (progress * duration);

                if (currentTime / 1000 == currentSecond / 1000) return;

                currentTime = currentSecond;
                progressTime.setText(
                        DeviceUtils.getDurationTimeMS(currentSecond) + "/" +
                                DeviceUtils.getDurationTimeMS(duration));
            });
        });
    }

    private void onVideoPlayerPrepared() {
        isSetup = true;
        imageView.setVisibility(View.GONE);

        Logger.logDebug(MediaPreviewViewHolder.class.getSimpleName(),
                "DiraVideoPlayer loaded");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (!fromUser) return;

                videoPlayer.setProgress((float) i / 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public interface ViewHolderActivityContract {

        void onWatchClicked(String messageId, long messageTime);

        void attachVideoPlayer(DiraVideoPlayer videoPlayer);

        boolean checkIsSelected(AttachmentMessagePair pair);

        void saveAttachment(String uri, boolean isVideo);

        int getActivityWidth();

        Member getMember(String id);
    }
}
