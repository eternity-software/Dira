package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.device.PerformanceClass;
import com.diraapp.device.PerformanceTester;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.DiraActivityListener;

public class QuickVideoPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    private MediaPlayer mediaPlayer;
    private String playingNow;

    private DiraActivity activity;
    private boolean attachedActivity = false;
    private boolean attachedRecycler = false;

    public QuickVideoPlayer(@NonNull Context context) {
        super(context);
        init();
    }

    public QuickVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void attachDiraActivity(DiraActivity diraActivity) {

        if (attachedActivity) return;
        activity = diraActivity;
        attachedActivity = true;
        diraActivity.addListener(new DiraActivityListener() {
            @Override
            public void onResume() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }
        });
    }

    public void attachRecyclerView(RecyclerView recyclerView) {

        if (attachedRecycler) return;
        attachedRecycler = true;

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (PerformanceTester.measureDevicePerformanceClass(getContext()) != PerformanceClass.POTATO)
                    return;
                try {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        play();
                    } else {
                        pause();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void pause() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.start();
            mediaPlayer.setVolume(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play(String source) {
        if (mediaPlayer == null) recreateMediaPlayer();
        if(source == null) return;
        playingNow = source;
        try {
            mediaPlayer.setDataSource(source);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    mediaPlayer.setVolume(0, 0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void setProgress(float progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) (progress * mediaPlayer.getDuration()));
        }
    }

    public void release() {
        if (mediaPlayer == null) return;
        mediaPlayer.reset();
        playingNow = null;
        mediaPlayer = null;
    }

    public void setSpeed(float speed) {
        if (mediaPlayer == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        try {
            if (mediaPlayer == null)
                recreateMediaPlayer();


            Surface surface = new Surface(surfaceTexture);
            mediaPlayer.setSurface(surface);
            play(playingNow);
        } catch (Exception e) {
            e.printStackTrace();
            recreateMediaPlayer();
        }


    }

    private void recreateMediaPlayer() {
        activity.runBackground(() -> {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setVolume(0, 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaPlayer.setAudioAttributes(
                        new AudioAttributes.Builder().setFlags(AudioAttributes.ALLOW_CAPTURE_BY_NONE).build());
            }
            mediaPlayer.setLooping(true);
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {

        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }
}
