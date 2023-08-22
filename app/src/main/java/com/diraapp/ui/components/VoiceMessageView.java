package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.ui.appearance.ColorTheme;
import com.masoudss.lib.WaveformSeekBar;

public class VoiceMessageView extends LinearLayout {

    private ColorTheme colorTheme;

    private boolean isSelfMessage;

    private boolean isInit = false;

    public VoiceMessageView(Context context, ColorTheme colorTheme, boolean isSelfMessage) {
        super(context);
        this.colorTheme = colorTheme;
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    public VoiceMessageView(@NonNull Context context) {
        super(context);
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_voice, this);

        ImageView playButton = findViewById(R.id.play_button);
        WaveformSeekBar bar = findViewById(R.id.waveform_seek_bar);

        if (isSelfMessage) {
            playButton.setColorFilter(colorTheme.getIconButtonColor());
            playButton.getBackground().setColorFilter(colorTheme.getIconButtonColor(), PorterDuff.Mode.SRC_IN);
        } else {
            playButton.setColorFilter(colorTheme.getRoomVoiceMessageColor());
            playButton.getBackground().setColorFilter(colorTheme.getAccentColor(), PorterDuff.Mode.SRC_IN);
        }
//        bar.setBackgroundColor(colorTheme.getUnreadMessageBackground());
//        bar.setWaveProgressColor(colorTheme.getAccentColor());
        isInit = true;
    }
}
