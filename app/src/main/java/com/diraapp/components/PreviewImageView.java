package com.diraapp.components;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * Easy-to-implement zoomable and slidable ImageView
 * <p>
 * For implementation you must create imageActionListener and a parent view
 * of image (next "ImageContainer")
 * <p>
 * That's all!
 *
 * @author Mikahil Karlov
 */
public class PreviewImageView extends FileParingImageView {

    private View imageContainer;
    private boolean isZoomed = false;
    private ImageActionsListener actionsListener;
    private boolean isAnimating = false;

    private boolean hasDown = false;
    private float downY = 0;
    private float downX = 0;
    private float zoomIncrease = 1f;
    private float defY = 0;
    private float defX = 0;
    private float height = 0;

    private long lastClickedTimestamp;

    public PreviewImageView(Context context) {
        super(context);
        init();
    }


    public PreviewImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

    public void setImageContainer(final View imageContainer) {
        this.imageContainer = imageContainer;

        imageContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if (isAnimating) {
                    hasDown = false;
                    // return true;
                }

                actionsListener.onTouch(event);
                float deltaY = (downY - event.getY());
                //  Log.d("onPreviewImageTouch", "deltaY " + deltaY + " y " + event.getY() + " downY:" + downY);
                float deltaX = (downX - event.getX());
                if (height == 0) height = getHeight();
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        downY = event.getY();
                        downX = event.getX();
                        hasDown = true;
                        // Log.d("PreviewImageView", "onTouch: DOWN *****************************");
                        return true;

                    case MotionEvent.ACTION_MOVE:


//                        Log.d("PREVIEW", "x=" + imageContainer.getX() + " downy=" + downY + " RAW_X=" + event.getRawX() +
//                                " RAW_y=" + event.getRawY() + " height=" + getHeight() + " width=" + imageContainer.getWidth());
                        float k = 1f;

                        if (!hasDown) return true;

                        if (!isZoomed) {
                            k = 1.5f;

                        }

                        float newY = (imageContainer.getY() - deltaY) / k;
                        float newX = (imageContainer.getX() - deltaX) / k;

                        if (isZoomed) {
                            imageContainer.setX(newX);
                        }

                        imageContainer.setY(newY);
                        //    Log.d("Preview imageview ", "new Y " + newY);
                        float delta = Math.abs(deltaY / (height / 2f));


                        if (delta > 1) delta = 1;
                        // Log.d("onPreviewImageTouch", "deltaY " + deltaY + " height " + height + " mmmmmmmmmmmmmmmm delta " + delta);
                        actionsListener.onSlide(delta * 0.5f);


                        break;
                    case MotionEvent.ACTION_UP:


                        if (!isZoomed) {
                            if (Math.abs(downY - event.getY()) > getHeight() / 16f) {
                                if (downY - event.getY() < 0) {
                                    actionsListener.onSlideDown();

                                } else {
                                    actionsListener.onSlideUp();
                                }

                            } else {
                                returnToDefaultPos();

                            }
                        } else {

                            if (event.getRawY() < 0) {
                                imageContainer.animate().y(0).setDuration(600).setInterpolator(new DecelerateInterpolator(5f)).start();
                            }
//                            if (Math.abs(downY - event.getRawY()) > imageContainer.getHeight() * 0.5) {
//                                Log.d("!", "y");
//                                returnToDefaultPos();
//                                isZoomed = false;
//                            }
                            if (Math.abs(imageContainer.getY()) > getHeight() * zoomIncrease * 0.5) {
                                returnToDefaultPos();
                                Log.d("!", "y");
                                isZoomed = false;
                            }
                            if (Math.abs(imageContainer.getX()) > imageContainer.getWidth() * zoomIncrease * 0.5) {
                                returnToDefaultPos();
                                Log.d("!", "x");
                                isZoomed = false;
                            }

                        }


                        if (isDoubleClick()) {

                            isZoomed = !isZoomed;
                            if (zoomIncrease == 2f) {
                                isZoomed = true;
                            }
                            if (isZoomed) {
                                zoomUpdate();
                                imageContainer.animate().y((imageContainer.getHeight() / 2f) - event.getY()).x((imageContainer.getWidth() / 2f / zoomIncrease) - event.getX()).scaleY(zoomIncrease).scaleX(zoomIncrease).setDuration(600).setInterpolator(new DecelerateInterpolator(5f))
                                        .setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                                actionsListener.onZoom(zoomIncrease);
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
                            } else {

                                returnToDefaultPos();
                            }
                        }
                        hasDown = false;
                        downX = 0;
                        downY = 0;
                        height = 0;
                        defX = 0;
                        defY = 0;
                        Log.d("PreviewImageView", "onTouch: UP =========================");


                    case MotionEvent.ACTION_CANCEL:
                        Log.d("PreviewImageView", "onTouch: CANCEL xxxxxxxxxxxxxxxxxxxxxxxxx");
                        break;
                }

                return true;

            }
        });
    }

    public void setActionsListener(ImageActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }

    public void returnToDefaultPos() {

        isZoomed = false;

        actionsListener.onReturn();
        zoomIncrease = 1f;


        // Использование строчки ниже ломает MotionEvent и делает дёрганными резкие движения перетаскивания по оси ординат
        // animate().x(defX).y(defY)
//        imageContainer.animate().scaleX(1f).scaleY(1f).setDuration(600).setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//
//               imageContainer.setX(defX);
//               imageContainer.setY(defY);
//               imageContainer.setTranslationX(defX);
//               imageContainer.setTranslationY(defY);
//               imageContainer.setPivotX(defX);
//               imageContainer.setPivotY(defY);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        }).setInterpolator(new DecelerateInterpolator(5f)).start();


        // Альтернативное рабочее решение
        ObjectAnimator animation = ObjectAnimator.ofFloat(imageContainer, "translationY", 0);


        ObjectAnimator animationX = ObjectAnimator.ofFloat(imageContainer, "translationX", 0);
        ObjectAnimator animationScaleX = ObjectAnimator.ofFloat(imageContainer, "scaleX", 1f);
        ObjectAnimator animationScaleY = ObjectAnimator.ofFloat(imageContainer, "scaleY", 1f);


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animationX, animation, animationScaleX, animationScaleY);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator(1f));
        animatorSet.start();


    }

    private boolean isDoubleClick() {
        if (System.currentTimeMillis() - lastClickedTimestamp < 200) {
            lastClickedTimestamp = System.currentTimeMillis();
            return true;
        } else {
            lastClickedTimestamp = System.currentTimeMillis();
            return false;
        }
    }

    private void zoomUpdate() {
        if (zoomIncrease == 1f) {
            zoomIncrease = 2f;
        } else if (zoomIncrease == 2f) {
            zoomIncrease = 3f;
        } else {
            zoomIncrease = 1f;
        }
    }


    private void init() {

        height = 0;

        actionsListener = new ImageActionsListener() {


            @Override
            public void onSlideDown() {

            }

            @Override
            public void onSlideUp() {

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
        };


    }

    public boolean isZoomed() {
        return isZoomed;
    }

    public interface ImageActionsListener {
        /**
         * Invokes on long image slide
         */


        void onSlideDown();

        void onSlideUp();

        void onReturn();


        void onZoom(float increase);

        void onSlide(float percent);

        void onTouch(final MotionEvent motionEvent);

        void onExitZoom();
    }


}
