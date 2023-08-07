package com.diraapp.media;

import static com.diraapp.storage.AppStorage.DIRA_FILES_PATH;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import com.diraapp.storage.AppStorage;

import java.io.File;
import java.io.IOException;

public class SoundRecorder {
    // This file is used to record voice
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    private Context context;

    public SoundRecorder(Context context) {
        this.context = context;
    }

    public void start() {

        if (mRecorder == null) {

            mRecorder = new MediaRecorder();


            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
            mRecorder.setAudioEncodingBitRate(128000);
            mRecorder.setAudioSamplingRate(32000);

            mRecorder.setOutputFile(getVoiceMessagePath());

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mRecorder.start();
            mEMA = 0.0;
        }
    }

    public String getVoiceMessagePath(){
        ContextWrapper cw = new ContextWrapper(context);

        File directory = cw.getDir(DIRA_FILES_PATH, Context.MODE_PRIVATE);

        return new File(directory, "voiceMessage.3gp").getPath();
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            MediaPlayer mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(getVoiceMessagePath());
                 mMediaPlayer.prepare();
                // mMediaPlayer.start();
            } catch (IOException e) {

            }

        }
    }

    public boolean isRunning() {
        return mRecorder != null;
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude()/2700.0);
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

}