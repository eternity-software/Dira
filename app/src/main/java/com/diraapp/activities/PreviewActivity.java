package com.diraapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.diraapp.R;
import com.diraapp.components.PreviewImageView;
import com.diraapp.components.VideoPlayer;
import com.diraapp.exceptions.VideoPlayerException;
import com.diraapp.storage.AppStorage;

public class PreviewActivity extends AppCompatActivity {

    public static final String URI = "uri";
    public static final String IS_VIDEO = "is_video";

    private VideoPlayer videoPlayer;
    private   PreviewImageView previewImageView;

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
                if(isVideo)
                {
                    addVideoToGallery(uri, getApplicationContext());
                }
                else
                {
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


        if(isVideo)
        {

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
                    public void onReady() {
                        try {
                            videoPlayer.play(uri);
                        } catch (VideoPlayerException e) {
                            e.printStackTrace();
                        }
                    }
                });


        }
        else
        {
            videoPlayer.setVisibility(View.GONE);
            previewImageView.setImageBitmap(AppStorage.getImage(uri));
        }

    }

    public void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void addVideoToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPlayer.release();
    }
}