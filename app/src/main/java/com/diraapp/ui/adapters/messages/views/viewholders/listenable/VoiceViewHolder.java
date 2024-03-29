package com.diraapp.ui.adapters.messages.views.viewholders.listenable;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.VoiceMessageView;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.utils.Logger;
import com.masoudss.lib.SeekBarOnProgressChanged;
import com.masoudss.lib.WaveformSeekBar;

import java.io.File;
import java.util.List;

public class VoiceViewHolder extends ListenableViewHolder {

    WaveformSeekBar waveformSeekBar;
    LinearLayout voiceLayout;
    ImageView playButton;

    VoiceMessageView voiceView;

    public VoiceViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

    }

    @Override
    public void clearProgress() {
        if (!isInitialized) return;

        setState(ListenableViewHolderState.UNSELECTED);
        waveformSeekBar.setProgress(0);

        voiceView.setPlayButton();
    }

    @Override
    public void setProgress(float progress) {
        if (!isInitialized) return;

        waveformSeekBar.setProgress(progress);
        Logger.logDebug("VoiceMessageViewHolder", "Progress changed");
    }

    @Override
    public void pause(boolean isPaused, float progress) {
        if (!isInitialized) return;

        if (isPaused) {
            setState(ListenableViewHolderState.PAUSED);
            waveformSeekBar.setProgress(progress);
            voiceView.setPlayButton();
            Logger.logDebug("VoiceMessageViewHolder", "VoiceMessageViewHolder - Paused");
        } else {
            setState(ListenableViewHolderState.PLAYING);
            voiceView.setPauseButton();
            Logger.logDebug("VoiceMessageViewHolder", "VoiceMessageViewHolder - Playing");
        }
    }

    @Override
    public void start() {
        if (!isInitialized) return;

        waveformSeekBar.setProgress(GlobalMediaPlayer.getInstance().getCurrentProgress());

        setState(ListenableViewHolderState.PLAYING);
        //pause icon

        voiceView.setPauseButton();
        Logger.logDebug("VoiceMessageViewHolder", "started");
    }

    @Override
    public void rebindPlaying(boolean isPaused, float progress) {
        if (!isInitialized) return;

        if (isPaused) {
            setState(ListenableViewHolderState.PAUSED);
            waveformSeekBar.setProgress(progress);
            voiceView.setPlayButton();
        } else {
            setState(ListenableViewHolderState.PLAYING);
            voiceView.setPauseButton();
        }
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        // holder.loading.setVisibility(View.GONE);

        getViewHolderManagerContract().getVoiceMessageThread().execute(() -> {
            try {
                // Check if media file is not corrupted
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getMessageAdapterContract().getContext(), Uri.fromFile(file));
                String dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                if (dur == null) return;
                if (dur.equals("0")) return;

                getViewHolderManagerContract().getAmplituda().processAudio(file)
                        .get(result -> {
                            try {
                                List<Integer> amplitudesData = result.amplitudesAsList();
                                int[] array = new int[amplitudesData.size()];
                                for (int i = 0; i < amplitudesData.size(); i++)
                                    array[i] = amplitudesData.get(i);


                                new Handler(Looper.getMainLooper()).post(() -> {
                                    try {
                                        waveformSeekBar.setSampleFrom(array);
                                    } catch (Exception ignored) {
                                    }
                                });
                            } catch (Exception e) {

                            }
                        }, exception -> {

                        });
            } catch (Exception e) {
                e.printStackTrace();
            }


        });

        waveformSeekBar.setVisibility(View.VISIBLE);
        //waveformSeekBar.setProgress(attachment.getVoiceMessageStopProgress());
        voiceLayout.setVisibility(View.VISIBLE);

        playButton.setOnClickListener((View v) -> {

            sendMessageListened(message);

            if (getState() != ListenableViewHolderState.UNSELECTED) {
                getMessageAdapterContract().currentListenablePaused(this);
            } else {
                getMessageAdapterContract().currentListenableStarted(this, file, 0);
            }
        });


        waveformSeekBar.setOnProgressChanged(new SeekBarOnProgressChanged() {
            @Override
            public void onProgressChanged(@NonNull WaveformSeekBar waveformSeekBar, float v, boolean fromUser) {
                if (!fromUser) return;

                if (getState() != ListenableViewHolderState.UNSELECTED) {
                    getMessageAdapterContract().currentListenableProgressChangedByUser(v, VoiceViewHolder.this);
                    return;
                }

                waveformSeekBar.setProgress(0);

            }
        });
    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        voiceView = new VoiceMessageView(itemView.getContext(), isSelfMessage);
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(voiceView);

        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
        playButton = itemView.findViewById(R.id.play_button);
        voiceLayout = itemView.findViewById(VoiceMessageView.VOICE_CONTAINER_ID);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        if (getState() == ListenableViewHolderState.UNSELECTED) {
            clearItem();
        }

        updateListeningIndicator(message.getSingleAttachment());

        if (!AttachmentDownloader.isAttachmentSaving(message.getSingleAttachment()))
            onAttachmentLoaded(message.getSingleAttachment(),
                    AttachmentDownloader.getFileFromAttachment(message.getSingleAttachment(),
                            itemView.getContext(), message.getRoomSecret()), message);
    }

    private void clearItem() {
        playButton.setOnClickListener((View v) -> {
        });

        clearProgress();
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
