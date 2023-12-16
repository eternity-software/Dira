package com.diraapp.ui.singlemediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GlobalMediaPlayer {

    private static GlobalMediaPlayer instance;

    private final ArrayList<GlobalMediaPlayerListener> listeners = new ArrayList<>();

    private final DiraMediaPlayer diraMediaPlayer = new DiraMediaPlayer();

    private File currentFile;

    private Message currentMessage = null;

    private float currentProgress = 0;

    private boolean isPaused = false;

    public static GlobalMediaPlayer getInstance() {
        if (instance == null) {
            instance = new GlobalMediaPlayer();
        }

        return instance;
    }

    public float getCurrentProgress() {
        return currentProgress;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void changePlyingMessage(@NonNull Message message, @NonNull File file) {

        currentMessage = message;
        currentProgress = 0;
        currentFile = file;
        isPaused = false;

        try {
            if (diraMediaPlayer.isPlaying()) {
                diraMediaPlayer.stop();
            }
            diraMediaPlayer.reset();
            diraMediaPlayer.setDataSource(file.getPath());

            diraMediaPlayer.prepareAsync();

            diraMediaPlayer.setOnPreparedListener((MediaPlayer mp) -> {
                notifyStarted();
                diraMediaPlayer.setOnProgressTick(() -> {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            currentProgress = 10 * diraMediaPlayer.getProgress();
                            notifyProgressChanged();

                            isPaused = !diraMediaPlayer.isPlaying();

                            message.getSingleAttachment().setVoiceMessageStopProgress(currentProgress);
                        } catch (Exception ignored) {
                        }
                    });
                });
                diraMediaPlayer.start();

                diraMediaPlayer.setOnPreparedListener(null);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPaused() {

        if (diraMediaPlayer.isPlaying()) {
            diraMediaPlayer.stop();
            Logger.logDebug("ffffffffffffsssss", "paused");
            isPaused = true;
        } else {
            diraMediaPlayer.start();
            isPaused = false;
        }
        notifyPause();
    }

    public void onClose() {
        // only for bar now
        if (diraMediaPlayer.isPlaying()) {
            diraMediaPlayer.stop();
        }
        diraMediaPlayer.reset();

        currentFile = null;
        currentMessage = null;
        currentProgress = 0;
        isPaused = false;

        notifyClosed();
    }



    private void notifyStarted() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onStart(currentMessage, currentFile);
        }
    }

    private void notifyProgressChanged() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onProgressChanged(currentProgress, currentMessage);
        }
    }

    private void notifyClosed() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onClose();
        }
    }

    private void notifyPause() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onPauseClicked(isPaused, currentProgress);
        }
    }

    public void release() {
        if (!diraMediaPlayer.isReleased()) {
            diraMediaPlayer.release();
        }
        instance = null;
    }

    public void registerListener(GlobalMediaPlayerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GlobalMediaPlayerListener listener) {
        listeners.remove(listener);
    }
}
