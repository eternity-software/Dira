package com.diraapp.media;

import android.media.MediaPlayer;

public class DiraMediaPlayer extends MediaPlayer {

    private boolean isReleased = false;

    private Runnable onProgressTick = null;

    private Thread progressThread;

    public DiraMediaPlayer()
    {
        progressThread = new Thread(() -> {
            while (!isReleased) {
                try {
                    Thread.sleep(50);

                    if(onProgressTick != null) {
                        onProgressTick.run();
                    }

                } catch (InterruptedException e) {

                }
            }

        });
        progressThread.start();
    }

    public void setOnProgressTick(Runnable onProgressTick) {
        this.onProgressTick = onProgressTick;
    }

    @Override
    public void reset() {
        super.reset();
        onProgressTick = null;
    }

    public void setProgress(float progress) {
        seekTo((int) (getDuration() * progress));
    }

    public float getProgress()
    {
        return  getCurrentPosition() / (float) getDuration();
    }

    @Override
    public void release() {
        super.release();
        isReleased = true;
    }
}
