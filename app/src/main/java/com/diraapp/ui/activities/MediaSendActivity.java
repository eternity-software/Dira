package com.diraapp.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Transition;

import android.transition.TransitionInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.util.Pair;

import com.diraapp.R;
import com.diraapp.storage.AppStorage;
import com.diraapp.transition.Transitions;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MediaSendActivity extends AppCompatActivity {

    public static final int CODE = 11;
    public static final int IMAGE_PURPOSE_SELECT = 1;
    public static final int IMAGE_PURPOSE_MESSAGE = 2;
    private static Bitmap imageBuffer;
    private String imageUri;
    private String finalImageUri;
    private Bitmap imageToSend;
    private TouchImageView imageView;
    private boolean isShown = false;
    private VideoPlayer videoPlayer;
    private boolean isVideo;

    public static void setImageBuffer(Bitmap imageBuffer) {
        MediaSendActivity.imageBuffer = imageBuffer;
    }

    public static void open(final Activity from, String imageUri, String text, final MediaGridItem mediaGridItem, int purpose) {
        Intent intent = new Intent(from, MediaSendActivity.class);
        intent.putExtra("uri", imageUri);
        intent.putExtra("text", text);
        intent.putExtra("purpose", purpose);
        intent.putExtra("type", mediaGridItem.getFileInfo().getMimeType());

        if (mediaGridItem.getFileInfo().isImage()) {
            setImageBuffer(mediaGridItem.getFileParingImageView().getBitmap());
        } else {
            setImageBuffer(mediaGridItem.getFileInfo().getVideoThumbnail());
        }

    /*    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                from,
                Pair.create(mediaGridItem, from.getString(R.string.transition_image_shared)));

        from.startActivityForResult(intent, CODE,
                options.toBundle());*/

        Window window = from.getWindow();
        View decor = window.getDecorView();
        View navigationBarStartView = decor.findViewById(android.R.id.navigationBarBackground);

        List<Pair<View, String>> pairs = new ArrayList<>();
        pairs.add(Pair.create(mediaGridItem, from.getString(R.string.transition_image_shared)));


        if (navigationBarStartView != null) {
            pairs.add(Pair.create(navigationBarStartView, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
        }



        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(from, pairs.toArray(new Pair[pairs.size()]));
        ActivityCompat.startActivity(from, intent, options.toBundle());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageUri = getIntent().getExtras().getString("uri");
        setContentView(R.layout.activity_media_send);
        imageView = findViewById(R.id.fileImageView);
        imageView.setImageContainer(findViewById(R.id.imageContainer));


        if (imageBuffer != null) {
            imageView.setImageBitmap(imageBuffer);
        }

        Transition sharedElementEnterTransition = TransitionInflater.from(this).inflateTransition(R.transition.image_shared_transition);

// Set the shared element enter transition
        getWindow().setSharedElementEnterTransition(sharedElementEnterTransition);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final String type = getIntent().getExtras().getString("type");
        final int imagePurpose = getIntent().getExtras().getInt("purpose");
        EditText editText = findViewById(R.id.message_box);
        ImageView editButton = findViewById(R.id.editButton);


        if (imagePurpose == IMAGE_PURPOSE_SELECT) {
            editText.setVisibility(View.INVISIBLE);
            ImageView sendButton = findViewById(R.id.sendButton);
            sendButton.setImageResource(R.drawable.ic_check);
        }

        videoPlayer = findViewById(R.id.videoView);
        editText.setText(getIntent().getExtras().getString("text"));
        finalImageUri = imageUri;
        if (!type.startsWith("image")) {
            editButton.setEnabled(false);
            isVideo = true;
            editButton.setVisibility(View.GONE);
            videoPlayer.setLoadingLayerEnabled(false);
        } else {

            videoPlayer.setVisibility(View.INVISIBLE);
        }




        DiraActivity.runGlobalBackground(() -> {
            try {
                Thread.sleep(1000); //wait for animation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Bitmap fullBitmap = AppStorage.getBitmapFromPath(finalImageUri, getApplicationContext());
            if (fullBitmap == null) return;
            runOnUiThread(() -> {
                if (!isDestroyed()) imageView.setImageBitmap(fullBitmap);
            });
        });

        imageView.setActionsListener(new TouchImageView.ImageActionsListener() {

            @Override
            public void onSlideDown() {
                imageView.returnToDefaultPos();
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


        getWindow().getSharedElementEnterTransition().setDuration(300);

        LinearLayout layout = findViewById(R.id.linearLayout3);
        layout.setVisibility(View.INVISIBLE);


        getWindow().getSharedElementEnterTransition().setInterpolator(new DecelerateInterpolator(2f));
        getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {

                if (isShown) return;
                isShown = true;
                showBottomBar();
                try {
                    videoPlayer.play(imageUri);
                    videoPlayer.setVolume(1);
                    if (type.startsWith("image")) {
                        DiraActivity.runGlobalBackground(() -> {
                            Bitmap fullsizeBitmap = AppStorage.getBitmapFromPath(finalImageUri, getApplicationContext());
                            if (fullsizeBitmap != null) {
                                runOnUiThread(() -> {
                                    imageView.setImageBitmap(fullsizeBitmap);
                                });
                            }
                        });
                    } else {

                        videoPlayer.setVideoPlayerListener(new VideoPlayer.VideoPlayerListener() {
                            @Override
                            public void onStarted() {
                                //   imageView.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onPaused() {

                            }

                            @Override
                            public void onReleased() {
                                imageView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onReady(int width, int height) {
                                try {
                                    videoPlayer.play(imageUri);
                                    videoPlayer.setVolume(1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {
                    if (Objects.equals(getResources().getString(R.string.transition_image_prepare), sharedElementNames.get(i))) {
                        View view = sharedElements.get(i);

                        if (view instanceof MediaGridItem) {
                            MediaGridItem mediaGridItem = (MediaGridItem) view;
                            mediaGridItem.appearContorllers();
                        }
                    }
                }
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {

                    View view = sharedElements.get(i);

                    if (view instanceof MediaGridItem) {
                        MediaGridItem mediaGridItem = (MediaGridItem) view;
                        mediaGridItem.appearContorllers();
                    }


                }
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            }
        });
        // overridePendingTransition(R.anim.slide_to_right, R.anim.slide_from_left);
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            //   adjustAspectRatio();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            // adjustAspectRatio();
        }
    }

    private void showBottomBar() {
        LinearLayout layout = findViewById(R.id.linearLayout3);
        layout.setVisibility(View.VISIBLE);
        Animation appearAnimation = AnimationUtils.loadAnimation(this,
                R.anim.appear_from_bottom);
        appearAnimation.setFillAfter(true);
        layout.startAnimation(appearAnimation);
    }

    public void editImageButtonClick(View v) {
        ImageEdit.openForResult(Uri.parse(finalImageUri), this);
    }

    @Override
    public void onBackPressed() {
        if (!imageView.isZoomed()) {
            super.onBackPressed();

            if (imageBuffer != null) {
                imageView.setImageBitmap(imageBuffer);
            }


            if (isVideo) {
                videoPlayer.release();
            }
            LinearLayout layout = findViewById(R.id.linearLayout3);
            Animation disappearAnimation = AnimationUtils.loadAnimation(this,
                    R.anim.hide_to_bottom);
            layout.startAnimation(disappearAnimation);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        } else {
            imageView.returnToDefaultPos();
        }

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null) return;
        if (resultCode == ImageEdit.RESULT_CODE) {
            finalImageUri = getRealPathFromURI(this, Uri.parse(data.getStringExtra("uri")));
            ImageView imageView = findViewById(R.id.fileImageView);
            imageView.setImageURI(Uri.parse(finalImageUri));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sendButtonClick(View v) {
        EditText editText = findViewById(R.id.message_box);

        videoPlayer.release();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        Intent intent = new Intent();
        ArrayList<String> strings = new ArrayList<>();
        strings.add(finalImageUri);
        intent.putExtra("uris", strings);
        intent.putExtra("text", editText.getText().toString());
        setResult(CODE, intent);
        finish();
    }


}