package com.diraapp.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.components.PreviewImageView;
import com.diraapp.components.VideoPlayer;
import com.diraapp.exceptions.VideoPlayerException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.images.ImagesWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PreviewActivity extends AppCompatActivity {

    public static final String URI = "uri";
    public static final String IS_VIDEO = "is_video";

    private VideoPlayer videoPlayer;
    private PreviewImageView previewImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        String uri = getIntent().getExtras().getString(URI);
        boolean isVideo = getIntent().getExtras().getBoolean(IS_VIDEO);

        previewImageView = findViewById(R.id.image_view);

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
                finish();
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


        if (isVideo) {
            previewImageView.setVisibility(View.GONE);
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
            previewImageView.setImageBitmap(AppStorage.getImage(uri));
        }

    }

    public void addImageToGallery(final String filePath, final Context context) {
        ImagesWorker.saveBitmapToGallery(AppStorage.getImage(filePath), this);
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
            // Get the already saved video as fileinputstream from here

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