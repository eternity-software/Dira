package com.diraapp.media;

import static com.diraapp.storage.AppStorage.DIRA_FILES_PATH;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaRecorder;

import java.io.File;

/**
 * Simple instrument that records voice
 */
public class SoundRecorder {
    static final private double EMA_FILTER = 0.6;
    private final Context context;
    private MediaRecorder mediaRecorder = null;
    private double ema = 0.0;

    public SoundRecorder(Context context) {
        this.context = context;
    }

    public void startRecording() {

        if (mediaRecorder == null) {

            mediaRecorder = new MediaRecorder();


            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(32000);

            mediaRecorder.setOutputFile(getVoiceMessagePath());

            try {
                mediaRecorder.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaRecorder.start();
            ema = 0.0;
        }
    }

    /**
     * Get the default voice message path
     *
     * @return
     */
    public String getVoiceMessagePath() {
        ContextWrapper contextWrapper = new ContextWrapper(context);

        File directory = contextWrapper.getDir(DIRA_FILES_PATH, Context.MODE_PRIVATE);

        return new File(directory, "voiceMessage.3gp").getPath();
    }

    public void stop() {
        try
        {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public boolean isRunning() {
        return mediaRecorder != null;
    }

    public double getAmplitude() {
        if (mediaRecorder != null)
            return (mediaRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;
    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        ema = EMA_FILTER * amp + (1.0 - EMA_FILTER) * ema;
        return ema;
    }

}