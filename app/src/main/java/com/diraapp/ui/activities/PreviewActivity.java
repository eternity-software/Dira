package com.diraapp.ui.activities;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.SharedElementCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.exceptions.VideoPlayerException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;

public class PreviewActivity extends DiraActivity {


    public static final String URI = "uri";
    public static final String IS_VIDEO = "is_video";
    public static final String EXTRA_CLIP_RECT = "rect";
    public static final String PREVIEW = "preview";
    private static Bitmap bitmapPool;
    private Bitmap currentBitmap;
    private Bitmap transitionBitmap;
    private VideoPlayer videoPlayer;
    private boolean isShown = false;
    private boolean isInTransition = false;
    private boolean isVideo = false;
    private TouchImageView touchImageView;
    private boolean usesSharedTransition;
    private String uri;


    public static void open(final DiraActivity from, String filePath, Bitmap previewImage, boolean isVideo, View transitionSource) {
        prepareActivity(from, filePath, previewImage, null, isVideo, transitionSource).start();
    }

    public static PreparedActivity prepareActivity(final DiraActivity from, String filePath, Bitmap previewImage, RecyclerView transitionRecyclerView, boolean isVideo, View transitionSource) {
        Intent intent = new Intent(from, PreviewActivity.class);
        intent.putExtra(PreviewActivity.URI, filePath);
        intent.putExtra(PreviewActivity.IS_VIDEO, isVideo);


        if (transitionRecyclerView != null) {
            // This is a simplest way to make proper clipping in transition
            transitionRecyclerView.setClipToPadding(true);
        }
        Rect localVisibleRect = new Rect();
        transitionSource.getLocalVisibleRect(localVisibleRect);


        if (transitionRecyclerView != null) {
            transitionRecyclerView.setClipToPadding(false);
        }
       /* localVisibleRect.top += DeviceUtils.dpToPx(12, from);
        localVisibleRect.bottom -= DeviceUtils.dpToPx(12, from);*/

        transitionSource.setClipBounds(localVisibleRect);
        transitionSource.setTransitionName(from.getString(R.string.transition_image_shared));
        intent.putExtra(PreviewActivity.EXTRA_CLIP_RECT, localVisibleRect);

        bitmapPool = previewImage;


        from.addListener(new DiraActivityListener() {
            @Override
            public void onResume() {
                transitionSource.setClipBounds(null);
                transitionSource.setTransitionName(null);
                from.removeListener(this);
            }
        });

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                from,
                Pair.create(transitionSource, from.getString(R.string.transition_image_shared)));


        return new PreparedActivity(from, intent, options);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        uri = getIntent().getExtras().getString(URI);
        isVideo = getIntent().getExtras().getBoolean(IS_VIDEO);


        touchImageView = findViewById(R.id.image_view);

        touchImageView.setImageBitmap(bitmapPool);


        transitionBitmap = bitmapPool;
        if (transitionBitmap != null)
            usesSharedTransition = true;
        else {
            usesSharedTransition = false;
            isShown = true;
        }
        bitmapPool = null;


        final String transitionName = getString(R.string.transition_image_shared);
        final Rect clipRect = getIntent().getParcelableExtra(EXTRA_CLIP_RECT);
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {

                isInTransition = true;
                for (int i = 0; i < sharedElementNames.size(); i++) {
                    if (Objects.equals(transitionName, sharedElementNames.get(i))) {
                        View view = sharedElements.get(i);
                        view.setClipBounds(clipRect);
                    }
                }
                CardView card = findViewById(R.id.card_view);

                ObjectAnimator animator;
                if (isShown) {

                    animator = ObjectAnimator.ofFloat(card, "radius", DeviceUtils.dpToPx(14, getApplicationContext()));

                } else {
                    isShown = true;
                    animator = ObjectAnimator.ofFloat(card, "radius", DeviceUtils.dpToPx(0, getApplicationContext()));

                }

                animator.setDuration(200);
                animator.start();
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {
                    if (Objects.equals(transitionName, sharedElementNames.get(i))) {
                        View view = sharedElements.get(i);
                        view.setClipBounds(null);
                    }
                }
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

                if (!isVideo)
                    touchImageView.setImageBitmap(transitionBitmap);
                isInTransition = false;
            }
        });

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideo) {
                    addVideoToGallery(uri, getApplicationContext());
                } else {
                    addImageToGallery(uri, getApplicationContext());
                }
                ImageView imageView = findViewById(R.id.save_button);
                imageView.setImageResource(R.drawable.ic_check);
            }
        });

        videoPlayer = findViewById(R.id.video_player);
        touchImageView.setImageContainer(findViewById(R.id.imageContainer));
        touchImageView.setActionsListener(new TouchImageView.ImageActionsListener() {
            @Override
            public void onSlideDown() {
                onBackPressed();
            }

            @Override
            public void onSlideUp() {
                onBackPressed();
            }

            @Override
            public void onReturn() {

            }

            @Override
            public void onZoom(float increase) {

            }

            @Override
            public void onSlide(float percent) {

            }

            @Override
            public void onTouch(MotionEvent motionEvent) {

            }

            @Override
            public void onExitZoom() {

            }
        });


        TextView sizeView = findViewById(R.id.size_view);
        sizeView.setText(AppStorage.getStringSize(new File(uri).length()));


        if (isVideo) {
            videoPlayer.setVideoPlayerListener(new VideoPlayer.VideoPlayerListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onPaused() {

                }

                @Override
                public void onReleased() {

                }

                @Override
                public void onReady(int width, int height) {
                    try {
                        videoPlayer.play(uri);
                        videoPlayer.setVolume(1);
                    } catch (VideoPlayerException e) {
                        e.printStackTrace();
                    }
                }
            });


        } else {
            videoPlayer.setVisibility(View.GONE);

        }


        getWindow().getSharedElementEnterTransition().setInterpolator(new DecelerateInterpolator(2f));
        getWindow().getSharedElementEnterTransition().setDuration(250);

    }

    @Override
    protected void onStart() {
        super.onStart();
        DiraActivity.runGlobalBackground(() -> {
            Bitmap bitmap = AppStorage.getBitmapFromPath(uri);
            if (isVideo) return;
            try {
                if (usesSharedTransition)
                    // Wait for animation
                    Thread.sleep(500);
            } catch (InterruptedException e) {

            }
            runOnUiThread(() -> {
                if (isDestroyed() | isInTransition) return;
                try {

                    PreviewActivity.this.currentBitmap = bitmap;
                    touchImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

    public void addImageToGallery(final String filePath, final Context context) {
        DiraActivity.runGlobalBackground(() -> {
            ImagesWorker.saveBitmapToGallery(AppStorage.getBitmapFromPath(filePath), this);
        });
    }

    public void addVideoToGallery(final String filePath, final Context context) {
        DiraActivity.runGlobalBackground(() -> {
            ImagesWorker.saveVideoToGallery(filePath, context);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        touchImageView.setImageBitmap(transitionBitmap);
        if (currentBitmap != null)
            currentBitmap.recycle();
        currentBitmap = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        touchImageView.setImageBitmap(null);
        videoPlayer.release();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        findViewById(R.id.imageContainer).setAlpha(0f);
    }
}