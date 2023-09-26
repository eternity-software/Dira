package com.diraapp.media;

import android.media.MediaPlayer;
import android.os.Build;

import com.diraapp.exceptions.ResetRequiredException;

import java.io.IOException;

public class DiraMediaPlayer extends MediaPlayer {

    private final Thread progressThread;
    private boolean isReleased = false;
    private Runnable onProgressTick = null;

    private boolean isResetRequired = false;

    public DiraMediaPlayer() {
        progressThread = new Thread(() -> {
            while (!isReleased) {
                try {
                    Thread.sleep(50);

                    if (onProgressTick != null) {
                        onProgressTick.run();
                    }

                } catch (InterruptedException e) {

                }
            }

        });
        progressThread.start();
    }


    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        if(isResetRequired)
            throw new ResetRequiredException();
        super.setDataSource(path);
        isResetRequired = true;
    }

    public void setOnProgressTick(Runnable onProgressTick) {
        this.onProgressTick = onProgressTick;
    }

    @Override
    public void reset() {
        super.reset();
        isResetRequired = false;
        onProgressTick = null;
    }

    public boolean isReleased() {
        return isReleased;
    }

    public float getProgress() {
        return getCurrentPosition() / (float) getDuration();
    }

    public void setProgress(float progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(progress == 0)
            {
                seekTo((int) 1,MediaPlayer.SEEK_CLOSEST);
                return;
            }
            seekTo((int) (getDuration() * progress),MediaPlayer.SEEK_CLOSEST);
        }
        else
        {
            if(progress == 0)
            {
                seekTo((int) 1);
                return;
            }
            seekTo((int) (getDuration() * progress));
        }
    }

    @Override
    public void release() {
        super.release();
        isReleased = true;
    }
}
