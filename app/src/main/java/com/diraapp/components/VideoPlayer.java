package com.diraapp.components;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.exceptions.VideoPlayerException;

import java.io.IOException;


public class VideoPlayer extends RelativeLayout implements TextureView.SurfaceTextureListener {

    private View rootView;
    private TextureView textureView;
    private RelativeLayout loadingView;
    private MediaPlayer mediaPlayer;
    private ImageView thumbNail;
    private boolean isInit = false;
    private boolean isLoadingLayerEnabled = false;
    private long delay = 0;
    private VideoPlayerListener videoPlayerListener;
    private String currentPlaying;

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public void setVideoPlayerListener(VideoPlayerListener videoPlayerListener) {
        this.videoPlayerListener = videoPlayerListener;
    }

    public void initViews() {
        if (isInit) return;


        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.video_player, this);
        textureView = findViewById(R.id.videoPlayerTextureView);
        loadingView = findViewById(R.id.videoPlayerLoadingView);
        thumbNail = findViewById(R.id.videoPlayerThumbnail);
        textureView.setSurfaceTextureListener(this);
        rootView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int leftWas, int topWas, int rightWas, int bottomWas) {
                if (mediaPlayer == null) return;
                try {
                    int widthWas = rightWas - leftWas; // Right exclusive, left inclusive
                    if (v.getWidth() != widthWas) {
                        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                    }
                    int heightWas = bottomWas - topWas; // Bottom exclusive, top inclusive
                    if (v.getHeight() != heightWas) {
                        adjustAspectRatio(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                    }
                } catch (Exception e) {

                }

            }
        });
        isInit = true;
    }

    public void setLoadingLayerEnabled(boolean loadingLayerEnabled) {
        isLoadingLayerEnabled = loadingLayerEnabled;
        if (loadingLayerEnabled) {
            loadingView.setVisibility(VISIBLE);
        } else {
            loadingView.setVisibility(INVISIBLE);
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {

            }

            if (videoPlayerListener != null) videoPlayerListener.onReleased();
        }
    }

    public void show(long animationDuration) {
        rootView.animate().alpha(1).setDuration(animationDuration).start();
    }

    public void hideLoading(long animationDuration) {
        loadingView.animate().alpha(0).setDuration(animationDuration).start();
    }

    @Override
    public View getRootView() {
        initViews();
        return rootView;
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {

        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v("VIDEO", "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        textureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);

        textureView.setTransform(txform);

    }

    public void setVideoThumbnail(String filePath) {
        thumbNail.setImageBitmap(ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND));
    }

    public void play(String filePath) throws VideoPlayerException {
        try {
            initViews();
            if (mediaPlayer == null) throw new VideoPlayerException();


            if (delay == 0) setVideoThumbnail(filePath);
            mediaPlayer.stop();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());
                    mediaPlayer.start();
                    thumbNail.setVisibility(INVISIBLE);
                    hideLoading(200);
                    if (videoPlayerListener != null) videoPlayerListener.onStarted();
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            adjustAspectRatio(width, height);

                        }
                    });
                }
            });
            currentPlaying = filePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer == null) return;
        mediaPlayer.pause();
        if (videoPlayerListener != null) videoPlayerListener.onPaused();
    }

    public void play() {
        if (mediaPlayer == null) return;
        mediaPlayer.start();
        if (videoPlayerListener != null) videoPlayerListener.onStarted();
    }

    public void setLooping(boolean isLooping) {
        if (mediaPlayer == null) return;
        mediaPlayer.setLooping(isLooping);
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        final Surface s = new Surface(surface);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {


                    Thread.sleep(delay);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mediaPlayer = new MediaPlayer();
                                setVolume(0);

                                mediaPlayer.setSurface(s);
                                mediaPlayer.setLooping(true);


                                if (currentPlaying != null) {
                                    play(currentPlaying);
                                }


//            mediaPlayer.setOnBufferingUpdateListener(this);
//            mediaPlayer.setOnCompletionListener(this);

                                if (videoPlayerListener != null) videoPlayerListener.onReady(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });


                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setVolume(volume, volume);
            } catch (Exception e) {

            }

        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    public interface VideoPlayerListener {
        void onStarted();

        void onPaused();

        void onReleased();

        void onReady(int width, int height);
    }
}
