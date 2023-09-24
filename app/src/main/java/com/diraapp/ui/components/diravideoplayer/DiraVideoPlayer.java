package com.diraapp.ui.components.diravideoplayer;

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
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.DiraActivityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiraVideoPlayer extends TextureView implements TextureView.SurfaceTextureListener {

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(1);
    private DiraMediaPlayer mediaPlayer = new DiraMediaPlayer();
    private Surface surface = null;
    private DiraVideoPlayerState state = DiraVideoPlayerState.RESET;
    private PlayingTask playingNow;
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

    public DiraVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DiraVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void attachDebugIndicator(View view) {
        debugIndicator = view;
    }

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

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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
                      //  pause();
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
        if(state != DiraVideoPlayerState.PREPARING && state != DiraVideoPlayerState.PAUSED)
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

    public void play(String source)
    {
        play(new PlayingTask(source));
    }
    public void play(PlayingTask source) {
        if (source == null) return;
        playingNow = source;


        Runnable play = new Runnable() {
            @Override
            public void run() {
                try {

                    if (source != playingNow) return;
                    if(mediaPlayer.isReleased()) return;
                    mediaPlayer.reset();
                    setupMediaPlayer(false);
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            if (source != playingNow) return;
                            play();
                        }
                    });
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

        DiraMediaPlayer mediaPlayerToDestroy = mediaPlayer;



        PlayingTask localPlayingNow = playingNow;

        threadPoolExecutor.execute(() -> {

                if(Objects.equals(localPlayingNow, playingNow)) {
                    mediaPlayer = null;
                    if(!mediaPlayerToDestroy.isReleased())
                    {
                        mediaPlayerToDestroy.reset();
                        mediaPlayerToDestroy.release();
                    }

                    playingNow = null;
                    notifyStateChanged(DiraVideoPlayerState.RESET);
                    setupMediaPlayer(true);
                }


        });




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
        try {
            Surface surface = new Surface(surfaceTexture);
            System.out.println("111");
            setSurface(surface);
            play(playingNow);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setSurface(@NonNull Surface surface) {

        try {
            this.surface = surface;
            if(mediaPlayer == null)
            {

                return;
            }
            mediaPlayer.setSurface(surface);
            notifyStateChanged(DiraVideoPlayerState.READY);
            mediaPlayer.start();




        } catch (Exception e) {
            e.printStackTrace();
        }


    }


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

                if(notifyState)
                {
                    notifyStateChanged(DiraVideoPlayerState.IDLE);
                }
                else
                {
                    state = DiraVideoPlayerState.IDLE;
                }

            if (surface != null) {
                    mediaPlayer.setSurface(surface);
                if(notifyState)
                {
                    notifyStateChanged(DiraVideoPlayerState.READY);
                }
                else
                {
                    state = DiraVideoPlayerState.READY;
                }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


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
                try {
                    listener.onStateChanged(diraVideoPlayerState);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });


    }

    public void onSurfaceGet(SurfaceTexture surfaceTexture)
    {
        Surface surface = new Surface(surfaceTexture);

        setSurface(surface);
        play(playingNow);
        threadPoolExecutor.execute(() -> {
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        onSurfaceGet(surfaceTexture);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {

        surface = null;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
      //  onSurfaceGet(surfaceTexture);
    }

    public DiraVideoPlayerState getState() {
        return state;
    }
}