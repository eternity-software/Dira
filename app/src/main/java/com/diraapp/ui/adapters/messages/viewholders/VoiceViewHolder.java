package com.diraapp.ui.adapters.messages.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.components.VoiceMessageView;
import com.masoudss.lib.WaveformSeekBar;

public class VoiceViewHolder extends TextMessageViewHolder {

    WaveformSeekBar waveformSeekBar;
    LinearLayout voiceLayout;
    ImageView playButton;

    public VoiceViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
        playButton = itemView.findViewById(R.id.play_button);
        voiceLayout = itemView.findViewById(VoiceMessageView.VOICE_CONTAINER_ID);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        View view = new VoiceMessageView(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        viewsContainer.addView(view);

        setInitialised(true);
        updateViews();
    }

    @Override
    public void onBind(Message message, Message previousMessage) {

    }

}
