package com.diraapp.ui.adapters.messages.views.viewholders;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
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
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.VoiceMessageView;
import com.masoudss.lib.SeekBarOnProgressChanged;
import com.masoudss.lib.WaveformSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VoiceViewHolder extends AttachmentViewHolder {

    WaveformSeekBar waveformSeekBar;
    LinearLayout voiceLayout;
    ImageView playButton;

    public VoiceViewHolder(@NonNull ViewGroup itemView,
                           MessageAdapterContract messageAdapterContract,
                           ViewHolderManagerContract viewHolderManagerContract,
                           boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);

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


        waveformSeekBar.setProgress(attachment.getVoiceMessageStopProgress());
        voiceLayout.setVisibility(View.VISIBLE);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    DiraMediaPlayer diraMediaPlayer = getViewHolderManagerContract().getDiraMediaPlayer();
                    if (diraMediaPlayer.isPlaying()) {
                        diraMediaPlayer.stop();
                    }
                    diraMediaPlayer.reset();
                    diraMediaPlayer.setDataSource(file.getPath());

                    diraMediaPlayer.prepareAsync();

                    diraMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            diraMediaPlayer.start();
                            waveformSeekBar.setOnProgressChanged(new SeekBarOnProgressChanged() {
                                @Override
                                public void onProgressChanged(@NonNull WaveformSeekBar waveformSeekBar, float v, boolean fromUser) {
                                    if (fromUser) {
                                        diraMediaPlayer.setProgress(v / 10);
                                    }
                                }
                            });

                            diraMediaPlayer.setOnProgressTick(new Runnable() {
                                @Override
                                public void run() {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                float progress = 10 * diraMediaPlayer.getProgress();
                                                waveformSeekBar.setProgress(progress);

                                                attachment.setVoiceMessageStopProgress(progress);
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    });
                                }
                            });


                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new VoiceMessageView(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);

        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
        playButton = itemView.findViewById(R.id.play_button);
        voiceLayout = itemView.findViewById(VoiceMessageView.VOICE_CONTAINER_ID);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        if (!AttachmentsStorage.isAttachmentSaving(message.getSingleAttachment()))
            onAttachmentLoaded(message.getSingleAttachment(),
                    AttachmentsStorage.getFileFromAttachment(message.getSingleAttachment(),
                            itemView.getContext(), message.getRoomSecret()), message);
    }

}
