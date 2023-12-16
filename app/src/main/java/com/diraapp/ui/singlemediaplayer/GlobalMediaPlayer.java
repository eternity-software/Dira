package com.diraapp.ui.singlemediaplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.ListenableViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.VoiceViewHolder;
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

    public void setCurrentProgress(VoiceViewHolder viewHolder, float progress) {
        diraMediaPlayer.setProgress(progress);
        currentProgress = progress;

        notifyStarted(viewHolder);
    }

    public void changePlyingMessage(@NonNull Message message, @NonNull File file, ListenableViewHolder viewHolder) {
        changePlyingMessage(message, file, viewHolder, 0, true);
    }

    public void changePlyingMessageWithoutClearing(@NonNull Message message, @NonNull File file,
                                    ListenableViewHolder viewHolder, float progress) {
        changePlyingMessage(message, file, viewHolder, progress, false);
    }

    public void changePlyingMessage(@NonNull Message message, @NonNull File file,
                                    ListenableViewHolder viewHolder, float progress,
                                    boolean clearPrevious) {

        currentMessage = message;
        currentProgress = progress;
        currentFile = file;
        isPaused = false;

        try {
            if (diraMediaPlayer.isPlaying()) {
                diraMediaPlayer.stop();
            }

            if (clearPrevious) notifyClosed();

            diraMediaPlayer.reset();
            diraMediaPlayer.setDataSource(file.getPath());

            diraMediaPlayer.prepareAsync();

            diraMediaPlayer.setOnPreparedListener((MediaPlayer mp) -> {
                notifyStarted(viewHolder);
                diraMediaPlayer.setOnProgressTick(() -> new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        currentProgress = 10 * diraMediaPlayer.getProgress();
                        notifyProgressChanged();

                        Logger.logDebug("GlobalMediaPlayer", "p = " + currentProgress);

                        if (currentProgress/10 == 1) {
                            notifyClosed();
                            // looks like it doesn't work correctly with short voice messages
                            Logger.logDebug("GlobalMediaPlayer", "ended");
                            diraMediaPlayer.stop();
                        }
                        isPaused = !diraMediaPlayer.isPlaying();

                        message.getSingleAttachment().setVoiceMessageStopProgress(currentProgress);

                        if (isPaused) {
                            diraMediaPlayer.setOnProgressTick(null);
                        }
                    } catch (Exception ignored) {
                    }
                }));


                diraMediaPlayer.setProgress(currentProgress / 10);
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
            Logger.logDebug("GlobalMediaPlayer", "paused");
            isPaused = true;
        } else {
            changePlyingMessage(currentMessage, currentFile, null, currentProgress, false);
            Logger.logDebug("GlobalMediaPlayer", "playing");
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



    private void notifyStarted(ListenableViewHolder viewHolder) {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onGlobalMediaPlayerStart(currentMessage, currentFile, viewHolder);
        }
    }

    private void notifyProgressChanged() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onGlobalMediaPlayerProgressChanged(currentProgress, currentMessage);
        }
    }

    private void notifyClosed() {
        for (GlobalMediaPlayerListener listener: listeners) {
            listener.onGlobalMediaPlayerClose();
        }
    }

    private void notifyPause() {
        for (GlobalMediaPlayerListener listener: listeners) {
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
