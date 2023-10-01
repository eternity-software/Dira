package com.diraapp.ui.components.diravideoplayer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
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
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.DiraActivityListener;
import com.diraapp.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DiraVideoPlayer is a powerful player, that
 * prevents lag when a lot of videos are playing
 */
public class DiraVideoPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
    private final List<DiraVideoPlayerListener> diraVideoPlayerListenerList = new ArrayList<>();
    private DiraMediaPlayer mediaPlayer = new DiraMediaPlayer();
    private Surface surface = null;
    private DiraVideoPlayerState state = DiraVideoPlayerState.RESET;
    private PlayingTask currentPlayingTask;
    private boolean attachedActivity = false;
    private boolean attachedRecycler = false;
    private View debugIndicator;


    public DiraVideoPlayer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DiraVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DiraVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initialization method
     * <p>
     * Create player and register surface listener
     *
     * @param context
     */
    private void init(Context context) {
        try {
            setSurfaceTextureListener(this);
            if (threadPoolExecutor == null) {
                int threadsCount = 2;
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
            setupMediaPlayer(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Attach view, that will be colored when player state changes
     *
     * @param view
     */
    public void attachDebugIndicator(View view) {
        debugIndicator = view;
    }

    /**
     * Attach activity for pausing player onPause
     *
     * @param diraActivity
     */
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

    /**
     * Attach recycler view to player for enabling scroll optimization
     *
     * @param recyclerView
     */
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

    /**
     * Ask player to pause current task
     */
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

    /**
     * Ask player to play (only if playing task existed)
     */
    public void play() {
        if (mediaPlayer == null) return;
        if (state != DiraVideoPlayerState.PREPARING && state != DiraVideoPlayerState.PAUSED)
            return;
        threadPoolExecutor.execute(() -> {
            try {

                mediaPlayer.setVolume(0, 0);
                mediaPlayer.start();

                if (PerformanceTester.measureDevicePerformanceClass(getContext()) == PerformanceClass.POTATO) {
                    setSpeed(0.3f);
                }
                notifyStateChanged(DiraVideoPlayerState.PLAYING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Play media directly from path
     *
     * @param source
     */
    @Deprecated
    public void play(String source) {
        play(new PlayingTask(source));
    }

    /**
     * Ask player to prepare media and play it as it ready
     *
     * @param source PlayingTask instance for single media file
     */
    public void play(PlayingTask source) {
        if (source == null) return;
        currentPlayingTask = source;


        // Logger.logDebug(getClass().getSimpleName(), "Queued " + currentPlayingTask.getSourcePath());
        Runnable play = new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.logDebug(getClass().getSimpleName(), "Playing " + currentPlayingTask.getSourcePath());
                    if (source != currentPlayingTask) return;
                    Logger.logDebug(getClass().getSimpleName(), "1 " + currentPlayingTask.getSourcePath());
                    if (mediaPlayer.isReleased()) {
                        return;
                    }
                    Logger.logDebug(getClass().getSimpleName(), "2 " + currentPlayingTask.getSourcePath());
                    mediaPlayer.reset();
                    setupMediaPlayer(false);

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            if (source != currentPlayingTask) return;
                            play();
                        }
                    });

                    // if(!isOnScreen()) return;

                    mediaPlayer.setDataSource(source.getSourcePath());
                    if (state == DiraVideoPlayerState.READY) {

                        notifyStateChanged(DiraVideoPlayerState.PREPARING);
                        mediaPlayer.prepare();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    /**
     * Add listener that will be destroyed after receives specific state
     *
     * @param listener
     */
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

    /**
     * Add listener for tracking DiraVideoPlayer states
     *
     * @param listener
     */
    public void addListener(DiraVideoPlayerListener listener) {
        diraVideoPlayerListenerList.add(listener);
    }

    /**
     * Remove listener for tracking DiraVideoPlayer states
     *
     * @param listener
     */
    public void removeListener(DiraVideoPlayerListener listener) {
        diraVideoPlayerListenerList.remove(listener);
    }

    /**
     * Seek to percent of media
     *
     * @param progress Float from 0 to 1
     */
    public void setProgress(float progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) (progress * mediaPlayer.getDuration()));
        }
    }

    /**
     * Stops current player
     */
    public void stop() {
        mediaPlayer.stop();
        notifyStateChanged(DiraVideoPlayerState.READY);
    }

    /**
     * Ask player to release and recreates MediaPlayer instance
     */
    public void reset() {
        if (mediaPlayer == null) return;
        if (state == DiraVideoPlayerState.IDLE) return;

        DiraMediaPlayer mediaPlayerToDestroy = mediaPlayer;


        PlayingTask localPlayingNow = currentPlayingTask;

        threadPoolExecutor.execute(() -> {

            if (Objects.equals(localPlayingNow, currentPlayingTask)) {
                //   mediaPlayer = null;
                surface = null;
                if (!mediaPlayerToDestroy.isReleased()) {
                    mediaPlayerToDestroy.reset();
                    //  mediaPlayerToDestroy.release();
                }

                currentPlayingTask = null;
                notifyStateChanged(DiraVideoPlayerState.RESET);

                // Auto-recreation after each reset
                setupMediaPlayer(true);
            }


        });


    }

    /**
     * Set playback speed (supported from Android M)
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } catch (Exception e) {

            }
        }
    }

    /**
     * System event that receives surface and passes it to DiraMediaPlayer
     *
     * @param surfaceTexture
     * @param i
     * @param i1
     */
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        try {
            Surface surface = new Surface(surfaceTexture);
            setSurface(surface);

            // In some cases in recycler view Surface appears after READY state.
            // Investigation need
            play(currentPlayingTask);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets surfaces directly to DiraMediaPlayer if it is available
     *
     * @param surface
     */
    private void setSurface(@NonNull Surface surface) {

        try {
            this.surface = surface;
            if (mediaPlayer == null) {

                return;
            }
            mediaPlayer.setSurface(surface);
            notifyStateChanged(DiraVideoPlayerState.READY);
            mediaPlayer.start();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public boolean isOnScreen() {

        if (!isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        getGlobalVisibleRect(actualPosition);
        final Rect screen = new Rect(0, 0, Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels);
        return actualPosition.intersect(screen);
    }

    /**
     * Create new DiraMediaPlayer and/or apply params to it
     * <p>
     * Can be executed without notifying state change
     *
     * @param notifyState
     */
    private void setupMediaPlayer(boolean notifyState) {

        try {
            if (mediaPlayer == null) mediaPlayer = new DiraMediaPlayer();
            //  if(state == DiraVideoPlayerState.PREPARING) return;
            mediaPlayer.setVolume(0, 0);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setFlags(
                        AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM).build());
            }
            mediaPlayer.setAudioStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mediaPlayer.setLooping(true);

            if (notifyState) {
                notifyStateChanged(DiraVideoPlayerState.IDLE);
            } else {
                state = DiraVideoPlayerState.IDLE;
            }

            if (surface != null) {
                mediaPlayer.setSurface(surface);
                if (notifyState) {
                    notifyStateChanged(DiraVideoPlayerState.READY);
                } else {
                    state = DiraVideoPlayerState.READY;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Notify listeners that player state has been changed
     *
     * @param diraVideoPlayerState
     */
    public void notifyStateChanged(DiraVideoPlayerState diraVideoPlayerState) {
        Logger.logDebug(getClass().getSimpleName(), "New state " + diraVideoPlayerState.name());
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
                try {
                    listener.onStateChanged(diraVideoPlayerState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    /**
     * Get current DiraVideoPlayer state
     *
     * @return
     */
    public DiraVideoPlayerState getState() {
        return state;
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

    }


}