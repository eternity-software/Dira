package com.diraapp.ui.activities;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.BorderScaleView;
import com.diraapp.ui.components.DrawingView;
import com.diraapp.utils.ImageRotationFix;
import com.diraapp.utils.Numbers;

import java.io.File;
import java.io.IOException;

public class ImageEdit extends DiraActivity {

    public final static int RESULT_CODE = 10;
    float startY = 0;
    float downY;
    boolean wasSliding = false;
    int[] colors;
    RelativeLayout cropContainer;
    int currentColor = 0;
    long lastTimeSizeChanged = 0;
    float realX;
    float realY;
    float goodX;
    float goodY;
    int goodHeight;
    int goodWeight;
    float bitmapContainerX;
    float bitmapContainerY;
    float fromX = 0;
    float dX = 0;
    float dY = 0;
    int startWidth = 0;
    private boolean isEraser = false;
    private boolean isCropping = false;
    private Bitmap imageBitmap;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public static void open(Uri uri, Activity activity) {
        Intent intent = new Intent(activity, ImageEdit.class);
        intent.putExtra("uri", uri.getPath());
        activity.startActivity(intent);
    }

    public static void openForResult(Uri uri, Activity activity) {
        Intent intent = new Intent(activity, ImageEdit.class);
        intent.putExtra("uri", uri.getPath());
        activity.startActivityForResult(intent, RESULT_CODE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
        final EditText editText = findViewById(R.id.text);

        String uri = getIntent().getExtras().getString("uri");
        final ImageView imageView = findViewById(R.id.mainImageView);
        try {
            imageBitmap = ImageRotationFix.handleSamplingAndRotationBitmapNoCropping(this, Uri.fromFile(new File(uri)));
            imageView.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }


        setupBrushEditor();
        setupCrop();
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {


                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();


                } else {
                    v.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                }

                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public void setupCrop() {


        final ImageView cropContainer = findViewById(R.id.cropShade);
        final ConstraintLayout bitmapContainer = findViewById(R.id.bitmapContainer);
        final ConstraintLayout mainContainer = findViewById(R.id.mainContainer);
        final ImageView imageView = findViewById(R.id.mainImageView);
        final BorderScaleView borderScaleView = findViewById(R.id.cropGrid);
        borderScaleView.setContainerHeight(mainContainer.getHeight());
        borderScaleView.setContainerWidth(mainContainer.getWidth());
        borderScaleView.setContainer(bitmapContainer);
        borderScaleView.setBorderScaleViewListener(new BorderScaleView.BorderScaleViewListener() {
            @Override
            public void onCordsChanged(float x, float y) {
                cropContainer.setX(x);
                cropContainer.setY(y);
            }

            @Override
            public void onSizeChanged(int width, int height) {
                cropContainer.getLayoutParams().width = width;
                cropContainer.getLayoutParams().height = height;
                cropContainer.requestLayout();

            }

            @Override
            public void onTouchUp(MotionEvent event) {
                float centerBorderX = mainContainer.getWidth() / 2f - borderScaleView.getWidth() / 2f;
                float centerBorderY = mainContainer.getHeight() / 2f - borderScaleView.getHeight() / 2f;


                float pathX = borderScaleView.getX() - centerBorderX;
                float pathY = borderScaleView.getY() - centerBorderY;


                realX = borderScaleView.getRawContainerX();
                realY = borderScaleView.getRawContainerY();


                borderScaleView.setText("realX=" + realX + "\nrealY=" + realY);
                if (borderScaleView.getRawContainerX() > 0 && borderScaleView.getRawContainerY() > 0 && borderScaleView.getRawContainerX() + borderScaleView.getWidth() <= bitmapContainer.getWidth() && borderScaleView.getRawContainerY() + borderScaleView.getHeight() <= bitmapContainer.getHeight()) {
                    goodWeight = borderScaleView.getWidth();
                    goodX = borderScaleView.getRawContainerX();
                    goodHeight = borderScaleView.getHeight();
                    goodY = borderScaleView.getRawContainerY();

                    bitmapContainer.animate().x(bitmapContainer.getX() - pathX).y(bitmapContainer.getY() - pathY).setInterpolator(new DecelerateInterpolator(2f)).setDuration(500).start();

                } else {
                    cropContainer.getLayoutParams().width = goodWeight;
                    cropContainer.getLayoutParams().height = goodHeight;
                    cropContainer.requestLayout();

                    borderScaleView.getLayoutParams().width = goodWeight;
                    borderScaleView.getLayoutParams().height = goodHeight;
                    borderScaleView.requestLayout();
                }

                borderScaleView.animate().x(centerBorderX).y(centerBorderY).setDuration(500).setInterpolator(new DecelerateInterpolator(2f)).start();
                cropContainer.animate().x(centerBorderX).y(centerBorderY).setDuration(500).setInterpolator(new DecelerateInterpolator(2f)).start();

                lastTimeSizeChanged = System.currentTimeMillis();
            }
        });


    }

    public void enablePaintingMode() {
        if (!isCropping) {
            final DrawingView drawingView = findViewById(R.id.drawingView);
            drawingView.setActive(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupBrushEditor() {
        colors = new int[]{Theme.getColor(this, R.color.paintWhite),
                Theme.getColor(this, R.color.paintBlack),
                Theme.getColor(this, R.color.paintRed),
                Theme.getColor(this, R.color.paintOrange),
                Theme.getColor(this, R.color.paintYellow),
                Theme.getColor(this, R.color.paintGreen),
                Theme.getColor(this, R.color.paintLightBlue),
                Theme.getColor(this, R.color.paintBlue),
                Theme.getColor(this, R.color.paintPurple)};
        final LinearLayout brushEditor = findViewById(R.id.brushEditor);
        final ImageView colorPreview = findViewById(R.id.colorPreview);
        final DrawingView drawingView = findViewById(R.id.drawingView);

        drawingView.setColor(colors[0]);

        brushEditor.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (startY == 0) {
                            startY = brushEditor.getTop();
                        }


                        downY = event.getRawY();
                        enablePaintingMode();
                        wasSliding = false;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        float offset = downY - event.getRawY();
                        int maxDpOffset = 200;
                        int maxDpSize = 100;
                        int minDpOffset = 2;
                        int maxPxOffset = Numbers.dpToPx(maxDpOffset, ImageEdit.this);
                        int maxPxSize = Numbers.dpToPx(maxDpSize, ImageEdit.this);
                        int minPxOffset = Numbers.dpToPx(minDpOffset, ImageEdit.this);
                        if (offset < maxPxOffset && minPxOffset < offset) {
                            brushEditor.setY(startY - offset);

                            float percentSize = Math.abs(offset) / maxPxSize;
                            float scaleFactor = (float) (1 + percentSize * 0.7);


                            drawingView.setSizePercentage(percentSize);
                            ((ImageView) findViewById(R.id.changeBrushButton)).setImageDrawable(getResources().getDrawable(R.drawable.ic_clean));
                            isEraser = false;
                            if (!wasSliding) {

                                wasSliding = true;

                            } else {


                            }
                            brushEditor.setScaleX(scaleFactor);
                            brushEditor.setScaleY(scaleFactor);

                        }


                        break;
                    case MotionEvent.ACTION_UP:
                        brushEditor.animate().y(startY).setDuration(300).setInterpolator(new DecelerateInterpolator(2f)).start();
                        ((ImageView) findViewById(R.id.changeBrushButton)).setImageDrawable(getResources().getDrawable(R.drawable.ic_clean));
                        isEraser = false;
                        if (!wasSliding) {
                            if (currentColor == colors.length - 1) {
                                currentColor = 0;
                            } else {
                                currentColor++;
                            }
                            drawingView.setColor(colors[currentColor]);
                            ImageViewCompat.setImageTintList(colorPreview, ColorStateList.valueOf(colors[currentColor]));


                        }
                        break;


                }
                return true;
            }
        });
    }

    public void changeBrush(View v) {
        final EditText editText = findViewById(R.id.text);
        editText.clearFocus();
        editText.setCursorVisible(false);
        enablePaintingMode();
        getWindow().getDecorView().clearFocus();


        getWindow().getDecorView().requestFocus();
        final DrawingView drawingView = findViewById(R.id.drawingView);
        if (isEraser) {
            isEraser = false;
            drawingView.setBrush();
            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_clean));
        } else {
            isEraser = true;
            drawingView.setEraser();
            ((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_brush));

        }
    }

    public void save(View v) {
        getWindow().getDecorView().clearFocus();
        getWindow().getDecorView().requestFocus();
        final EditText editText = findViewById(R.id.text);
        editText.setCursorVisible(false);
        final ConstraintLayout container = findViewById(R.id.bitmapContainer);
        container.setDrawingCacheEnabled(true);
        Bitmap b = container.getDrawingCache();
        String result = MediaStore.Images.Media.insertImage(getContentResolver(), b, "CUTE_EDITED" + System.currentTimeMillis(), "Cute photo editor");

        Intent intent = new Intent();
        intent.putExtra("uri", result);
        setResult(RESULT_CODE, intent);

        finish();
    }

    public void crop() {

        ImageView mainImageView = findViewById(R.id.mainImageView);
        ImageView shadedView = findViewById(R.id.cropShade);
        DrawingView drawingView = findViewById(R.id.drawingView);
        BorderScaleView borderScaleView = findViewById(R.id.cropGrid);

        int x = (int) (imageBitmap.getWidth() * (borderScaleView.getRawContainerX() / mainImageView.getWidth()));
        int y = (int) (imageBitmap.getHeight() * (borderScaleView.getRawContainerY() / mainImageView.getHeight()));

        float factor = (float) imageBitmap.getWidth() / (float) mainImageView.getWidth();

        int width = (int) (borderScaleView.getWidth() * factor);
        int height = (int) (borderScaleView.getHeight() * factor);

        Log.d("CROP", "x= " + x + "; width=" + imageBitmap.getWidth() + " factor " + (borderScaleView.getX() / mainImageView.getWidth()));
        Bitmap croppedBitmap = imageBitmap;
        try {


            croppedBitmap = Bitmap.createBitmap(imageBitmap, x, y, width, height);
        } catch (Exception ignored) {
        }

        borderScaleView.center();
        final ConstraintLayout bitmapContainer = findViewById(R.id.bitmapContainer);
        bitmapContainer.setX(bitmapContainerX);
        bitmapContainer.setY(bitmapContainerY);
        bitmapContainer.requestLayout();

        mainImageView.setImageBitmap(croppedBitmap);
        bitmapContainer.requestLayout();
        imageBitmap = croppedBitmap;
    }

    public void text(View v) {
        final EditText editText = findViewById(R.id.text);
        editText.setCursorVisible(true);
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                     keyboard.showSoftInput(editText, 0);
                                 }
                             }
                , 200);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final EditText editText = findViewById(R.id.text);
        editText.clearFocus();
        getWindow().getDecorView().clearFocus();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public void toggleCrop(View v) {
        ImageView mainImageView = findViewById(R.id.mainImageView);
        ImageView shadedView = findViewById(R.id.cropShade);
        DrawingView drawingView = findViewById(R.id.drawingView);
        BorderScaleView borderScaleView = findViewById(R.id.cropGrid);
        final ConstraintLayout bitmapContainer = findViewById(R.id.bitmapContainer);
        final ConstraintLayout mainContainer = findViewById(R.id.mainContainer);
        LinearLayout applyButton = findViewById(R.id.linearLayout9);
        if (isCropping) {
            bitmapContainer.animate().alpha(1f).setDuration(300).start();
            borderScaleView.setEnabled(false);
            applyButton.setVisibility(View.VISIBLE);
            borderScaleView.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    borderScaleView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).setDuration(300).start();

            shadedView.animate().alpha(0).setDuration(300).start();
            drawingView.setActive(true);
            drawingView.setEnabled(true);
            isCropping = false;
            crop();
        } else {

            applyButton.setVisibility(View.GONE);
            bitmapContainerX = bitmapContainer.getX();
            bitmapContainerY = bitmapContainer.getY();
            borderScaleView.setContainerHeight(mainContainer.getHeight());
            borderScaleView.setContainerWidth(mainContainer.getWidth());
            bitmapContainer.setAlpha(0.7f);
            borderScaleView.setEnabled(true);
            borderScaleView.center();
            goodWeight = borderScaleView.getWidth();
            goodX = borderScaleView.getRawContainerX();
            goodHeight = borderScaleView.getHeight();
            goodY = borderScaleView.getRawContainerY();
            borderScaleView.setVisibility(View.VISIBLE);
            borderScaleView.setAlpha(0f);
            borderScaleView.animate().alpha(1f).setDuration(300).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
            shadedView.animate().alpha(1f).setDuration(300).start();
            drawingView.setActive(false);
            drawingView.setEnabled(true);
            isCropping = true;

        }

//        CropImageView cropImageView = findViewById(R.id.crop_view);
//        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//        cropImageView.setImageBitmap(bitmap);
//        cropImageView.setScaleType(CropImageView.ScaleType.CENTER_INSIDE);
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            cropContainer.invalidate();
            return true;
        }
    }
}
