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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.BuildConfig;
import com.diraapp.R;
import com.diraapp.device.PerformanceClass;
import com.diraapp.device.PerformanceTester;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.DiraActivityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiraVideoPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    private static ExecutorService threadPoolExecutor;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Surface surface = null;
    private DiraVideoPlayerState state = DiraVideoPlayerState.RESET;
    private String playingNow;
    private boolean attachedActivity = false;
    private boolean attachedRecycler = false;

    private View debugIndicator;
    private final List<DiraVideoPlayerListener> diraVideoPlayerListenerList = new ArrayList<>();

    public DiraVideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DiraVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void attachDebugIndicator(View view) {
        debugIndicator = view;
    }

    private void init(Context context) {
        setSurfaceTextureListener(this);
        if (threadPoolExecutor == null) {
            int threadsCount = 1;
            switch (PerformanceTester.measureDevicePerformanceClass(context)) {
                case HIGH:
                    threadsCount = 6;
                    break;
                case MEDIUM:
                    threadsCount = 3;
                    break;
            }
            threadPoolExecutor = Executors.newFixedThreadPool(threadsCount);
        }
        setupMediaPlayer();
    }

    public void attachDiraActivity(DiraActivity diraActivity) {

        if (attachedActivity) return;
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

    public void pause() {
        if (mediaPlayer == null) return;
        threadPoolExecutor.execute(() -> {
            try {
                mediaPlayer.pause();
                state = DiraVideoPlayerState.PAUSED;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void play() {
        if (mediaPlayer == null) return;
        threadPoolExecutor.execute(() -> {
            try {
                mediaPlayer.start();
                mediaPlayer.setVolume(0, 0);

                if (PerformanceTester.measureDevicePerformanceClass(getContext()) == PerformanceClass.POTATO) {
                    setSpeed(0.3f);
                }
                notifyStateChanged(DiraVideoPlayerState.PLAYING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void play(String source) {
        if (source == null) return;
        playingNow = source;

        setupMediaPlayer();

        Runnable play = () -> {
            try {

                if (!source.equals(playingNow)) return;

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        if (source.equals(playingNow))
                            play();


                    }
                });
                if (state == DiraVideoPlayerState.READY) {
                    mediaPlayer.setDataSource(source);
                    notifyStateChanged(DiraVideoPlayerState.PREPARING);
                    mediaPlayer.prepare();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        };

        if (state == DiraVideoPlayerState.READY) {
            threadPoolExecutor.execute(play);
        } else {

            addSelfDestroyListener(state -> {
                if (state == DiraVideoPlayerState.READY) {
                    threadPoolExecutor.execute(play);
                    return true;
                }
                return false;

            });
        }
    }

    public void removeListener(DiraVideoPlayerListener listener) {
        diraVideoPlayerListenerList.remove(listener);
    }

    public void addSelfDestroyListener(DiraVideoPlayerListener listener) {
        diraVideoPlayerListenerList.add(new DiraVideoPlayerListener() {
            @Override
            public boolean onStateChanged(DiraVideoPlayerState diraVideoPlayerState) {
                try {
                    if (listener.onStateChanged(diraVideoPlayerState)) {
                        removeListener(this);
                        return true;
                    }
                } catch (Exception e) {
                    removeListener(this);
                    e.printStackTrace();
                    return true;
                }


                return false;
            }
        });
    }

    public void addListener(DiraVideoPlayerListener listener) {
        diraVideoPlayerListenerList.add(listener);
    }


    public void setProgress(float progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) (progress * mediaPlayer.getDuration()));
        }
    }

    public void stop() {
        mediaPlayer.stop();
        notifyStateChanged(DiraVideoPlayerState.READY);
    }

    public void reset() {
        if (mediaPlayer == null) return;

        MediaPlayer mediaPlayerToDestroy = mediaPlayer;

        mediaPlayer = null;

        threadPoolExecutor.execute(() -> {
            mediaPlayerToDestroy.reset();
            mediaPlayerToDestroy.release();
            notifyStateChanged(DiraVideoPlayerState.RESET);
        });

        playingNow = null;


    }

    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        threadPoolExecutor.execute(() -> {
            try {
                Surface surface = new Surface(surfaceTexture);
                setSurface(surface);
                play(playingNow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void setSurface(@NonNull Surface surface) {

        try {
            this.surface = surface;
            if(mediaPlayer == null)
            {
                setupMediaPlayer();
                return;
            }
            mediaPlayer.setSurface(surface);
            notifyStateChanged(DiraVideoPlayerState.READY);



        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void setupMediaPlayer() {
        threadPoolExecutor.execute(() -> {

            try {
                if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
                mediaPlayer.setVolume(0, 0);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setFlags(
                            AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM).build());
                }
                mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
                mediaPlayer.setLooping(true);
                notifyStateChanged(DiraVideoPlayerState.IDLE);
                if (surface != null) {
                    setSurface(surface);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public void notifyStateChanged(DiraVideoPlayerState diraVideoPlayerState) {
        state = diraVideoPlayerState;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (debugIndicator != null && BuildConfig.DEBUG) {
                if (state == DiraVideoPlayerState.READY) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintGreen));
                } else if (state == DiraVideoPlayerState.PREPARING) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintOrange));
                } else if (state == DiraVideoPlayerState.IDLE) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintBlue));
                } else if (state == DiraVideoPlayerState.RESET) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintRed));
                } else if (state == DiraVideoPlayerState.PAUSED) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintYellow));
                } else if (state == DiraVideoPlayerState.PLAYING) {
                    debugIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.paintPurple));
                }

            }
            for (DiraVideoPlayerListener listener : new ArrayList<>(diraVideoPlayerListenerList)) {
                listener.onStateChanged(diraVideoPlayerState);
            }
        });


    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {

        surface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        threadPoolExecutor.execute(() -> {
            try {
                Surface surface = new Surface(surfaceTexture);
                setSurface(surface);
                //   play(playingNow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DiraVideoPlayerState getState() {
        return state;
    }
}