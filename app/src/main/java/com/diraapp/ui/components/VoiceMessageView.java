package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.utils.android.DeviceUtils;
import com.masoudss.lib.WaveformSeekBar;

public class VoiceMessageView extends LinearLayout {

    public static final int VOICE_CONTAINER_ID = 642377;
    private boolean isSelfMessage;

    private boolean isPlayButtonActive = true;

    private int playButtonColor;

    private ImageView playButton;


    public VoiceMessageView(Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    public VoiceMessageView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {

        try {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            View root = inflater.inflate(R.layout.message_voice, this);

            playButton = findViewById(R.id.play_button);
            WaveformSeekBar bar = findViewById(R.id.waveform_seek_bar);
            LinearLayout indicator = findViewById(R.id.listened_indicator);

            int marginHorizontal = DeviceUtils.dpToPx(8, getContext());
            LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            params.setMargins(marginHorizontal, 0, marginHorizontal, 0);

            setLayoutParams(params);

            setPadding(0, DeviceUtils.dpToPx(2, getContext()), 0,
                    DeviceUtils.dpToPx(4, getContext()));

            setId(VOICE_CONTAINER_ID);

            int waveBackgroundColor = Theme.getColor(getContext(), R.color.message_waves_background);
            int wavesColor = Theme.getColor(getContext(), R.color.message_waves);
            playButtonColor = Theme.getColor(getContext(), R.color.message_voice_play);
            int playColorBackground = Theme.getColor(getContext(), R.color.message_voice_play_background);
            int indicatorColor = Theme.getColor(getContext(), R.color.message_voice_play);

            if (isSelfMessage) {
                waveBackgroundColor = Theme.getColor(getContext(), R.color.self_message_waves_background);
                wavesColor = Theme.getColor(getContext(), R.color.self_message_waves);
                playButtonColor = Theme.getColor(getContext(), R.color.self_message_voice_play);
                playColorBackground = Theme.getColor(getContext(), R.color.self_message_voice_play_background);
                indicatorColor = Theme.getColor(getContext(), R.color.self_message_voice_play);
            }

            playButton.getBackground().setColorFilter(playColorBackground, PorterDuff.Mode.SRC_ATOP);
            playButton.setColorFilter(playButtonColor);

            bar.setWaveBackgroundColor(waveBackgroundColor);
            bar.setWaveProgressColor(wavesColor);

            indicator.getBackground().setColorFilter(indicatorColor, PorterDuff.Mode.SRC_ATOP);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setPlayButton() {
        if (isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = true;
    }

    public void setPauseButton() {
        if (!isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = false;
    }
}
