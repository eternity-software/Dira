package com.diraapp.ui.activities;

import android.app.Activity;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;

import com.diraapp.R;
import com.diraapp.transition.Transitions;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.ImageRotationFix;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ImageSendActivity extends DiraActivity {

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
        ImageSendActivity.imageBuffer = imageBuffer;
    }

    public static void open(final Activity from, String imageUri, String text, final FilePreview fileParingImageView, int purpose) {
        Intent intent = new Intent(from, ImageSendActivity.class);
        intent.putExtra("uri", imageUri);
        intent.putExtra("text", text);
        intent.putExtra("purpose", purpose);
        intent.putExtra("type", fileParingImageView.getFileInfo().getMimeType());

        if (fileParingImageView.getFileInfo().isImage()) {
            setImageBuffer(fileParingImageView.getFileParingImageView().getBitmap());
        } else {
            setImageBuffer(fileParingImageView.getFileInfo().getVideoThumbnail());
        }


        from.startActivityForResult(intent, CODE,
                Transitions.makeOneViewTransition(fileParingImageView.getFileParingImageView(), from, intent, from.getResources().getString(R.string.transition_image_prepare)));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageUri = getIntent().getExtras().getString("uri");
        setContentView(R.layout.activity_media_send);
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
        Bitmap bitmap = getIntent().getParcelableExtra("bitmap");

        if (!type.startsWith("image")) {
            editButton.setEnabled(false);
            isVideo = true;
            editButton.setVisibility(View.GONE);
            videoPlayer.setLoadingLayerEnabled(false);
        } else {

            videoPlayer.setVisibility(View.INVISIBLE);
        }

        imageView = findViewById(R.id.fileImageView);
        imageView.setImageContainer(findViewById(R.id.imageContainer));


        if (imageBuffer != null) {
            imageView.setImageBitmap(imageBuffer);
        }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                            final Bitmap fixedBitmap = ImageRotationFix.handleSamplingAndRotationBitmapNoCropping(ImageSendActivity.this, Uri.fromFile(new File(imageUri)));
                            imageView.setImageBitmap(fixedBitmap);
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
        } else {
            showBottomBar();
        }

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {
                    if (Objects.equals(getResources().getString(R.string.transition_image_prepare), sharedElementNames.get(i))) {
                        View view = sharedElements.get(i);

                        if (view instanceof FilePreview) {
                            FilePreview filePreview = (FilePreview) view;
                            filePreview.appearContorllers();
                        }
                    }
                }
                super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                for (int i = 0; i < sharedElementNames.size(); i++) {

                    View view = sharedElements.get(i);

                    if (view instanceof FilePreview) {
                        FilePreview filePreview = (FilePreview) view;
                        filePreview.appearContorllers();
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