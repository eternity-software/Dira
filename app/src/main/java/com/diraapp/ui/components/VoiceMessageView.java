package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.appearance.ColorTheme;
import com.masoudss.lib.WaveformSeekBar;

public class VoiceMessageView extends LinearLayout {

    private boolean isSelfMessage;

    private boolean isInit = false;


    public VoiceMessageView(Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    public VoiceMessageView(@NonNull Context context) {
        super(context);
    }

    private void initView() {


        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_voice, this);

        ImageView playButton = findViewById(R.id.play_button);
        WaveformSeekBar bar = findViewById(R.id.waveform_seek_bar);

        int waveBackgroundColor = Theme.getColor(getContext(), R.color.message_waves_background);
        int wavesColor = Theme.getColor(getContext(), R.color.message_waves);
        int playColor = Theme.getColor(getContext(), R.color.message_voice_play);
        int playColorBackground = Theme.getColor(getContext(), R.color.message_voice_play_background);

        if(isSelfMessage)
        {
             waveBackgroundColor = Theme.getColor(getContext(), R.color.self_message_waves_background);
             wavesColor = Theme.getColor(getContext(), R.color.self_message_waves);
             playColor = Theme.getColor(getContext(), R.color.self_message_voice_play);
             playColorBackground = Theme.getColor(getContext(), R.color.self_message_voice_play_background);
        }

        try {
            playButton.getBackground().setColorFilter(playColorBackground, PorterDuff.Mode.SRC_ATOP);
            playButton.setColorFilter(playColor);
            bar.setWaveBackgroundColor(waveBackgroundColor);
            bar.setWaveProgressColor(wavesColor);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        isInit = true;
    }
}
