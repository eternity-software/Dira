package com.diraapp.ui.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.SharedElementCallback;

import com.diraapp.R;
import com.diraapp.exceptions.VideoPlayerException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.transition.Transitions;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.ui.components.PreviewImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.Numbers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;

public class PreviewActivity extends DiraActivity {

    public static final String URI = "uri";
    public static final String IS_VIDEO = "is_video";
    public static final String EXTRA_CLIP_RECT = "rect";
    private VideoPlayer videoPlayer;
    private boolean isShown = false;
    private PreviewImageView previewImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        String uri = getIntent().getExtras().getString(URI);
        boolean isVideo = getIntent().getExtras().getBoolean(IS_VIDEO);

        previewImageView = findViewById(R.id.image_view);

        if(isVideo)
        {
            previewImageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(uri, MediaStore.Video.Thumbnails.MINI_KIND));
        }

        Transition transition =
                TransitionInflater.from(this)
                        .inflateTransition(R.transition.image_shared_transition);
        getWindow().setSharedElementEnterTransition(transition);

        final String transitionName = getString(R.string.transition_image_shared);
        final Rect clipRect = getIntent().getParcelableExtra(EXTRA_CLIP_RECT);
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {
                    if (Objects.equals(transitionName, sharedElementNames.get(i))) {
                        View view = sharedElements.get(i);
                        view.setClipBounds(clipRect);
                    }
                }
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
        previewImageView.setImageContainer(findViewById(R.id.imageContainer));
        previewImageView.setActionsListener(new PreviewImageView.ImageActionsListener() {
            @Override
            public void onSlideDown() {
                previewImageView.returnToDefaultPos();
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
        getWindow().getSharedElementEnterTransition()
                .addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                        CardView card = findViewById(R.id.card_view);

                        ObjectAnimator animator;
                        if(isShown)
                        {

                            animator = ObjectAnimator.ofFloat(card, "radius", Numbers.dpToPx(14, getApplicationContext()));

                        }
                        else
                        {
                            isShown = true;
                            animator = ObjectAnimator.ofFloat(card, "radius", Numbers.dpToPx(0, getApplicationContext()));

                        }

                        animator.setDuration(100);
                        animator.start();
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {

                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });




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
            previewImageView.setImageBitmap(AppStorage.getBitmapFromPath(uri));
        }

    }

    public static void open(final DiraActivity from, String filePath, boolean isVideo, View transitionSource) {
        Intent intent = new Intent(from, PreviewActivity.class);
        intent.putExtra(PreviewActivity.URI, filePath);
        intent.putExtra(PreviewActivity.IS_VIDEO, isVideo);

        Rect localVisibleRect = new Rect();
        transitionSource.getLocalVisibleRect(localVisibleRect);
        transitionSource.setClipBounds(localVisibleRect);
        transitionSource.setTransitionName(from.getString(R.string.transition_image_shared));

        intent.putExtra(PreviewActivity.EXTRA_CLIP_RECT, localVisibleRect);

        from.addListener(new DiraActivityListener() {
            @Override
            public void onResume() {
                transitionSource.setClipBounds(null);
                from.removeListener(this);
            }
        });

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                from,
                Pair.create(transitionSource, from.getString(R.string.transition_image_shared)));
        from.startActivity(intent, options.toBundle());
    }

    public void addImageToGallery(final String filePath, final Context context) {
        ImagesWorker.saveBitmapToGallery(AppStorage.getBitmapFromPath(filePath), this);
    }

    public void addVideoToGallery(final String filePath, final Context context) {
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";

        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder");
        valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
        valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        valuesvideos.put(
                MediaStore.Video.Media.DATE_ADDED,
                System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri collection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uriSavedVideo = resolver.insert(collection, valuesvideos);
        ParcelFileDescriptor pfd;

        try {
            pfd = getContentResolver().openFileDescriptor(uriSavedVideo, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            File imageFile = new File(filePath);
            FileInputStream in = new FileInputStream(imageFile);

            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
            pfd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        valuesvideos.clear();
        valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
        context.getContentResolver().update(uriSavedVideo, valuesvideos, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}