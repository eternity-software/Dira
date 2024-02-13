package com.diraapp.ui.adapters.roominfo.voice;

import android.graphics.ColorFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.res.Theme;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.ListenableViewHolderState;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.ui.fragments.roominfo.voice.VoiceFragmentAdapterContract;
import com.diraapp.utils.Logger;

public class VoiceAttachmentViewHolder extends RecyclerView.ViewHolder {

    public TextView mainText;

    public TextView typeText;

    public ImageView playButton;

    public ThemeLinearLayout playBackground;

    public ImageView watchButton;

    private AttachmentMessagePair pair;

    private ListenableViewHolderState state = ListenableViewHolderState.UNSELECTED;

    private final int playButtonColor;

    private boolean isPlayButtonActive = true;

    private VoiceFragmentAdapterContract.ViewClickListener viewClickListener;

    public VoiceAttachmentViewHolder(@NonNull View itemView,
                                     VoiceFragmentAdapterContract.ViewClickListener viewClickListener) {
        super(itemView);

        mainText = itemView.findViewById(R.id.main_text);
        typeText = itemView.findViewById(R.id.type_text);
        playButton = itemView.findViewById(R.id.play_icon);
        playBackground = itemView.findViewById(R.id.play_background);
        watchButton = itemView.findViewById(R.id.watch);

        playButtonColor = Theme.getColor(itemView.getContext(), R.color.message_voice_play);

        this.viewClickListener = viewClickListener;
    }

    public AttachmentMessagePair getPair() {
        return pair;
    }

    public void bind(AttachmentMessagePair attachmentMessagePair) {
        pair = attachmentMessagePair;

        mainText.setText(pair.getMessage().getShortAuthorNickname());

        AttachmentType type = pair.getAttachment().getAttachmentType();
        if (type == AttachmentType.VOICE) {
            typeText.setText(itemView.getContext().getString(R.string.message_type_voice));
        } else if (type == AttachmentType.BUBBLE) {
            typeText.setText(itemView.getContext().getString(R.string.message_type_bubble));
        } else {
            typeText.setText(type.name());
        }

        playBackground.setOnClickListener((View v) -> {
            if (state == ListenableViewHolderState.UNSELECTED) {
                viewClickListener.onViewStartClicked(this);
            } else {
                viewClickListener.onCurrentViewClicked();
            }
        });

    }

    public void setPlayButton() {
        if (isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_play));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = true;
    }

    public void setPauseButton() {
        if (!isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_pause));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = false;
    }

    public void start() {
        Logger.logDebug(VoiceAttachmentViewHolder.class.getSimpleName(), "Started");
        state = ListenableViewHolderState.PLAYING;
        setPauseButton();
    }

    public void pause(boolean isPaused) {
        if (isPaused) {
            Logger.logDebug(VoiceAttachmentViewHolder.class.getSimpleName(), "Paused");
            state = ListenableViewHolderState.PAUSED;
            setPlayButton();
        } else {
            Logger.logDebug(VoiceAttachmentViewHolder.class.getSimpleName(), "Continued");
            state = ListenableViewHolderState.PLAYING;
            setPauseButton();
        }
    }

    public void close() {
        Logger.logDebug(VoiceAttachmentViewHolder.class.getSimpleName(), "Closed");
        setPlayButton();
        state = ListenableViewHolderState.UNSELECTED;
    }

    public void onResume(boolean isPaused) {
        Logger.logDebug(VoiceAttachmentViewHolder.class.getSimpleName(), "Resumed");
        pause(isPaused);
    }

//    public void release() {
//        viewClickListener = null;
//    }
}