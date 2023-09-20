package com.diraapp.ui.components.quickvideoplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String playingNow;

    private DiraActivity activity;
    private boolean attachedActivity = false;
    private boolean attachedRecycler = false;

    private boolean isReadyToPlay = true;

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
        if (source == null) return;
        if (mediaPlayer == null) {
            PlayerRecreatedCallback callback = new PlayerRecreatedCallback() {
                @Override
                public void onRecreated() {
                    playMediaPlayerSource(source);
                }
            };
            recreateMediaPlayer(callback);
            return;
        }

        playMediaPlayerSource(source);
    }

    private void playMediaPlayerSource(String source) {
        playingNow = source;
        DiraActivity.runGlobalBackground(() -> {
        try {

                if (source == null) return;
                if (!isReadyToPlay) {
                    mediaPlayer.reset();
                    setupMediaPlayer();
                }

                isReadyToPlay = false;
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    mediaPlayer.setVolume(0, 0);

                }
            });
                mediaPlayer.setDataSource(source);
                mediaPlayer.prepare();



        } catch (Exception e) {
            e.printStackTrace();

        }
        });
    }

    public void setProgress(float progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) (progress * mediaPlayer.getDuration()));
        }
    }

    public void reset() {
        if (mediaPlayer == null) return;
        MediaPlayer destroyMediaPlayer = mediaPlayer;
        mediaPlayer = null;

        DiraActivity.runGlobalBackground(() -> {
            destroyMediaPlayer.reset();
            destroyMediaPlayer.release();

        });

        isReadyToPlay = true;
        playingNow = null;

    }

    public void setSpeed(float speed) {
        if (mediaPlayer == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            }
            catch (Exception e)
            {

            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        try {
            if (mediaPlayer == null) {
                recreateMediaPlayer(new PlayerRecreatedCallback() {
                    @Override
                    public void onRecreated() {
                        setSurfaceOnMediaPlayer(surfaceTexture);
                    }
                });
                return;
            }

            setSurfaceOnMediaPlayer(surfaceTexture);
        } catch (Exception e) {
            e.printStackTrace();
            recreateMediaPlayer();
        }
    }

    private void setSurfaceOnMediaPlayer(@NonNull SurfaceTexture surfaceTexture) {
        try {
            Surface surface = new Surface(surfaceTexture);
            mediaPlayer.setSurface(surface);
            play(playingNow);
        } catch (Exception e) {
            e.printStackTrace();
            recreateMediaPlayer();
        }
    }

    private void recreateMediaPlayer() {
        recreateMediaPlayer(null);
    }

    private void recreateMediaPlayer(PlayerRecreatedCallback callback) {
        DiraActivity.runGlobalBackground(() -> {
            mediaPlayer = new MediaPlayer();
            setupMediaPlayer();

            if (callback != null) {
                callback.onRecreated();
            }
        });
    }

    private void setupMediaPlayer()
    {
        if(mediaPlayer == null) return;
        mediaPlayer.setVolume(0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setFlags(
                    AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM).build());
        }
        mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
        mediaPlayer.setLooping(true);
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
