package com.diraapp.ui.singlemediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.utils.Logger;
import com.diraapp.utils.StringFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GlobalMediaPlayer {

    private static GlobalMediaPlayer instance;

    private final ArrayList<GlobalMediaPlayerListener> listeners = new ArrayList<>();

    private final DiraMediaPlayer diraMediaPlayer = new DiraMediaPlayer();

    private File currentFile;

    private Message currentMessage = null;

    /**
     * Current progress is float -> [0, 10]
     * Same progress values Waveform gives
     */
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


    public boolean setCurrentProgress(float currentProgress) {
        if (!isActive()) return false;
        if (currentProgress == this.currentProgress) return true;

        this.currentProgress = currentProgress;
        if (isPlaying()) diraMediaPlayer.setProgress(currentProgress / 10);

        notifyProgressChanged();

        return true;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isActive() {
        return currentMessage != null;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public Message getCurrentMessage() {
        return currentMessage;
    }


    public void changePlyingMessage(@NonNull Message message, @NonNull File file, float progress) {

        currentMessage = message;
        currentProgress = progress;
        currentFile = file;
        isPaused = false;

        try {
            if (diraMediaPlayer.isPlaying()) {
                diraMediaPlayer.stop();
            }

            diraMediaPlayer.reset();
            diraMediaPlayer.setDataSource(file.getPath());

            diraMediaPlayer.setOnPreparedListener((MediaPlayer mp) -> {
                notifyStarted();
                diraMediaPlayer.setOnProgressTick(() -> new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        currentProgress = 10 * diraMediaPlayer.getProgress();
                        notifyProgressChanged();

                        Logger.logDebug("GlobalMediaPlayer", "p = " + currentProgress);

                        isPaused = !diraMediaPlayer.isPlaying();

                        message.getSingleAttachment().setVoiceMessageStopProgress(currentProgress);

                        if (isPaused) {
                            diraMediaPlayer.setOnProgressTick(null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

                setOnCompletionListener();

                diraMediaPlayer.setProgress(currentProgress / 10);
                diraMediaPlayer.start();

                diraMediaPlayer.setOnPreparedListener(null);
            });

            diraMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onPaused() {
        if (!isActive()) {
            onClose();
            return;
        }

        if (diraMediaPlayer.isPlaying()) {
            diraMediaPlayer.stop();
            Logger.logDebug("GlobalMediaPlayer", "paused");
            isPaused = true;
        } else {
            changePlyingMessage(currentMessage, currentFile, currentProgress);
            Logger.logDebug("GlobalMediaPlayer", "playing");
            isPaused = false;
        }
        notifyPause();
    }

    public void onClose() {
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

    public boolean isPlaying() {
        return diraMediaPlayer.isPlaying();
    }

    private void setOnCompletionListener() {
        final String thisMessageId;
        if (currentMessage == null) {
            thisMessageId = StringFormatter.EMPTY_STRING;
        } else {
            thisMessageId = currentMessage.getId();
        }
        diraMediaPlayer.setOnCompletionListener((MediaPlayer mediaPlayer) -> {
            if (currentMessage != null && !thisMessageId.equals(StringFormatter.EMPTY_STRING)) {
                if (!currentMessage.getId().equals(thisMessageId)) return;
            }
            currentMessage = null;
            currentProgress = 0;
            isPaused = true;
            currentFile = null;
            diraMediaPlayer.stop();
            diraMediaPlayer.setOnProgressTick(null);
            diraMediaPlayer.setOnCompletionListener(null);
            notifyClosed();

            Logger.logDebug(GlobalMediaPlayer.class.getName(), "Listenable completed");
        });
    }

    private void notifyStarted() {
        for (GlobalMediaPlayerListener listener : listeners) {
            listener.onGlobalMediaPlayerStart(currentMessage, currentFile);
        }
    }

    private void notifyProgressChanged() {
        for (GlobalMediaPlayerListener listener : listeners) {
            listener.onGlobalMediaPlayerProgressChanged(currentProgress, currentMessage);
        }
    }

    private void notifyClosed() {
        for (GlobalMediaPlayerListener listener : listeners) {
            listener.onGlobalMediaPlayerClose();
        }
    }

    private void notifyPause() {
        for (GlobalMediaPlayerListener listener : listeners) {
            listener.onGlobalMediaPlayerPauseClicked(isPaused, currentProgress);
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
