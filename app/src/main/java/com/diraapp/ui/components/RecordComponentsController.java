package com.diraapp.ui.components;

import static com.diraapp.storage.AppStorage.DIRA_FILES_PATH;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.diraapp.R;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.media.SoundRecorder;
import com.diraapp.res.Theme;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Mode;
import com.r0adkll.slidr.model.SlidrInterface;

import java.io.File;

/**
 * Controller for recording bubbles and voice messages
 * Provides animations and requires specific UI components
 */
public class RecordComponentsController {

    private final ImageView recordButton;
    private final ImageView recordRipple;
    private final DiraActivity context;
    private final CameraView camera;
    private final View recordBubbleLayout;
    private final View recordingStatusBar;
    private final CardView bubbleContainer;
    private final TextView recordingStatusText;
    private SoundRecorder soundRecorder;
    private long lastTimeRecordButtonDown = 0;
    private long lastTimeRecordButtonUp = 0;
    private boolean isRecordButtonVisible = true;

    private View recordIndicator;
    private RecordListener recordListener;
    private float lastScale = 1;

    private boolean isSaving = true;

    private SlidrInterface slidrInterface;

    private boolean isRecording = false;
    private boolean isPreparingCamera = true;

    private TextView recordingTipText;

    private int secondsRecording = 0;

    public RecordComponentsController(ImageView recordButton,
                                      ImageView recordRipple,
                                      TextView recordingStatusText,
                                      TextView recordingTipText,
                                      View recordIndicator,
                                      DiraActivity context,
                                      LinearLayout recordingStatusBar,
                                      CameraView camera,
                                      SlidrInterface slidrInterface,
                                      View recordBubbleLayout,
                                      CardView bubbleContainer) {
        this.recordButton = recordButton;
        this.recordRipple = recordRipple;
        this.context = context;
        this.recordingStatusText = recordingStatusText;
        this.recordIndicator = recordIndicator;
        this.recordingTipText = recordingTipText;
        this.camera = camera;
        this.recordingStatusBar = recordingStatusBar;
        this.slidrInterface = slidrInterface;
        this.recordBubbleLayout = recordBubbleLayout;
        this.bubbleContainer = bubbleContainer;

        camera.setEnabled(false);
        soundRecorder = new SoundRecorder(context);

        camera.setLifecycleOwner(null);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(VideoResult result) {
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Bubble captured");
                if (recordListener != null && isSaving && secondsRecording > 1) {
                    recordListener.onMediaMessageRecorded(result.getFile().getPath(), AttachmentType.BUBBLE);

                }
                camera.close();
                camera.setLifecycleOwner(null);
            }

            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                super.onCameraOpened(options);
                System.out.println("Camera opened");
                if (recordBubbleLayout.getVisibility() == View.GONE) {
                    camera.close();
                }
            }
        });

        camera.setMode(Mode.VIDEO);
        camera.close();

        recordBubbleLayout.setVisibility(View.GONE);
        initRecordButton();
    }

    public void setRecordListener(RecordListener recordListener) {
        this.recordListener = recordListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initRecordButton() {
        initRecordType();

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastTimeRecordButtonDown = System.currentTimeMillis();
                    Handler handler = new Handler(Looper.getMainLooper());
                    final long localTimeDown = lastTimeRecordButtonDown;
                    handler.postDelayed(() -> {
                        if (lastTimeRecordButtonUp < lastTimeRecordButtonDown
                                && lastTimeRecordButtonDown == localTimeDown) {
                            // record
                            isSaving = true;
                            isRecording = true;
                            slidrInterface.lock();
                            CacheUtils cacheUtils = context.getCacheUtils();
                            boolean isVoiceRecord = cacheUtils.getBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT);
                            if (!isVoiceRecord) {
                                isPreparingCamera = true;
                                recordBubble();
                            }

                            recordingStatusBar.setVisibility(View.VISIBLE);
                            AlphaAnimation animation = new AlphaAnimation(0, 1);
                            animation.setDuration(100);
                            recordingStatusBar.startAnimation(animation);

                            AlphaAnimation recordAnimation = new AlphaAnimation(0, 1);
                            recordAnimation.setDuration(500);
                            recordAnimation.setRepeatMode(Animation.REVERSE);
                            recordAnimation.setRepeatCount(Animation.INFINITE);

                            recordIndicator.startAnimation(recordAnimation);

                            recordListener.onMediaMessageRecordingStart(AttachmentType.VOICE);
                            initVoiceIndicator();
                            recordingTipText.setText(context.getString(R.string.recording_tip_send));

                        }
                    }, 30);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    lastTimeRecordButtonUp = System.currentTimeMillis();

                    if (lastTimeRecordButtonUp - lastTimeRecordButtonDown < 100) {
                        CacheUtils cacheUtils = new CacheUtils(context);
                        boolean isVoiceRecord = cacheUtils.getBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT);
                        cacheUtils.setBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT, !isVoiceRecord);
                        initRecordType();
                    } else {

                        soundRecorder.stop();
                        CacheUtils cacheUtils = context.getCacheUtils();
                        boolean isVoiceRecord = cacheUtils.getBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT);
                        if (!isVoiceRecord) {
                            camera.stopVideo();
                            context.performScaleAnimation(1f, 0, bubbleContainer).setAnimationListener(
                                    new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    }
                            );

                            AlphaAnimation animation = new AlphaAnimation(1, 0);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    recordBubbleLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            animation.setDuration(100);
                            recordBubbleLayout.startAnimation(animation);

                        } else {
                            if (recordListener != null && isSaving && secondsRecording > 1) {
                                recordListener.onMediaMessageRecorded(soundRecorder.getVoiceMessagePath(),
                                        AttachmentType.VOICE);
                            }

                        }
                        isRecording = false;

                        AlphaAnimation animation = new AlphaAnimation(1, 0);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                recordingStatusBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        animation.setDuration(100);
                        recordingStatusBar.startAnimation(animation);
                        slidrInterface.unlock();
                        int colorFrom = Theme.getColor(context, R.color.accent);
                        recordButton.getAnimation().cancel();
                        recordButton.getBackground().setColorFilter(colorFrom, PorterDuff.Mode.SRC_ATOP);
                        lastTimeRecordButtonDown = 0;
                        lastTimeRecordButtonUp = 0;
                        context.performScaleAnimation(1.4f, 1, recordButton);
                        context.performScaleAnimation(lastScale, 0, recordRipple);
                    }
                }

                int maxOffset = DeviceUtils.dpToPx(40, context);


                System.out.println("posis " + recordButton.getX() + " " + recordButton.getY() + " motion " + event.getX() + " " + event.getY());
                if (Math.abs(event.getX()) < maxOffset && Math.abs(event.getY()) < maxOffset) {
                    if (!isSaving) {
                        isSaving = true;
                        recordingTipText.setText(context.getString(R.string.recording_tip_send));
                        int colorFrom = Theme.getColor(context, R.color.red);
                        int colorTo = Theme.getColor(context, R.color.accent);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(100); // milliseconds
                        colorAnimation.addUpdateListener((animator) -> {
                                recordRipple.getBackground().setColorFilter((Integer) animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                                recordButton.getBackground().setColorFilter((Integer) animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                        });

                        colorAnimation.start();
                    }
                } else {
                    if (isSaving) {
                        isSaving = false;
                        int colorFrom = Theme.getColor(context, R.color.accent);
                        recordingTipText.setText(context.getString(R.string.recording_tip_stop));

                        int colorTo = Theme.getColor(context, R.color.red);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(100); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                recordRipple.getBackground().setColorFilter((Integer) animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                                recordButton.getBackground().setColorFilter((Integer) animator.getAnimatedValue(), PorterDuff.Mode.SRC_ATOP);
                            }

                        });
                        colorAnimation.start();
                    }
                }

                return true;
            }
        });
    }

    public void recordBubble() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA},
                    1);
            return;
        }
        camera.setEnabled(true);
        ContextWrapper cw = new ContextWrapper(context);

        File directory = cw.getDir(DIRA_FILES_PATH, Context.MODE_PRIVATE);

        if (recordListener != null) {
            recordListener.onMediaMessageRecordingStart(AttachmentType.BUBBLE);
        }

        recordBubbleLayout.setVisibility(View.VISIBLE);
        context.performScaleAnimation(0.5f, 1, bubbleContainer);
        camera.close();
        camera.setLifecycleOwner(context);
        camera.open();


        camera.addCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                super.onCameraOpened(options);
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Taking captured video...");
                camera.takeVideoSnapshot(new File(directory, "bubbleMessage.mp4"));
                isPreparingCamera = false;
                camera.removeCameraListener(this);
            }
        });

        // binding.camera.open();
    }

    public void vibrateRecording() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
        soundRecorder.startRecording();
    }

    public void initVoiceIndicator() {


        secondsRecording = 0;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1);
            return;
        }

        Thread timer = new Thread(() -> {
            long lastTimeCheck = 0;
            // Note: do not use sleep here
            while (isRecording) {
                try {

                    if (System.currentTimeMillis() - lastTimeCheck > 1000) {
                        int time = secondsRecording * 1000;
                        int minutes = time / (60 * 1000);
                        int seconds = (time / 1000) % 60;
                        String secondsString = String.valueOf(seconds);

                        if (seconds < 10) {
                            secondsString = "0" + seconds;
                        }
                        secondsRecording++;
                        String finalSecondsString = secondsString;
                        context.runOnUiThread(() -> {
                            if (isPreparingCamera) {
                                recordingStatusText.setText(context.getString(R.string.recording_preparing));
                                secondsRecording = 0;
                            } else {

                                recordingStatusText.setText(context.getString(R.string.recording_prefix) + " " + minutes + ":" + finalSecondsString);
                            }
                        });
                        lastTimeCheck = System.currentTimeMillis();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timer.start();

        context.performScaleAnimation(1, 1.4f, recordButton);

        vibrateRecording();
        context.performScaleAnimation(0, 2, recordRipple).setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lastScale = 2;
                Runnable pollTask = new Runnable() {
                    @Override
                    public void run() {
                        if (!soundRecorder.isRunning()) return;
                        try {
                            double amplitude = soundRecorder.getAmplitude();


                            float scale = 2;
                            if (amplitude > 5) {
                                amplitude = 5;
                            }


                            scale += (float) amplitude / 3;


                            float finalScale = scale;

                            ScaleAnimation scaleOut = new ScaleAnimation(lastScale, scale,
                                    lastScale, scale, Animation.RELATIVE_TO_SELF, 0.5f,
                                    Animation.RELATIVE_TO_SELF, 0.5f);
                            scaleOut.setDuration(50);
                            scaleOut.setInterpolator(new DecelerateInterpolator());


                            scaleOut.setFillAfter(true);

                            recordRipple.startAnimation(scaleOut);
                            scaleOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    lastScale = finalScale;
                                    run();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        } catch (Exception e) {
                            soundRecorder = new SoundRecorder(context);
                            e.printStackTrace();
                        }
                    }
                };
                pollTask.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    /**
     * Prepare user's preferred recording mode
     */
    public void initRecordType() {
        CacheUtils cacheUtils = context.getCacheUtils();
        boolean isVoiceRecord = cacheUtils.getBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT);
        if (isVoiceRecord) {
            recordButton.setImageDrawable(context.getDrawable(R.drawable.ic_mic));
        } else {
            recordButton.setImageDrawable(context.getDrawable(R.drawable.ic_bubble));

        }
    }

    public void handleInputAnimation(boolean showRecordButton, View sendButton) {

        if (showRecordButton) {
            if (!isRecordButtonVisible) {
                isRecordButtonVisible = true;
                context.performScaleAnimation(0, 1, recordButton);
                recordButton.setEnabled(true);
                context.performScaleAnimation(1, 0, sendButton);
            }

        } else {
            if (isRecordButtonVisible) {
                isRecordButtonVisible = false;
                recordButton.setEnabled(false);
                context.performScaleAnimation(recordButton.getScaleX(), 0, recordButton);
                context.performScaleAnimation(0, 1, sendButton);
            }
        }
    }

    public interface RecordListener {
        /**
         * Called when a bubble or voice message recorded
         *
         * @param path           Path to file
         * @param attachmentType BUBBLE or VOICE
         */
        void onMediaMessageRecorded(String path, AttachmentType attachmentType);

        default void onMediaMessageRecordingStart(AttachmentType attachmentType) {
        }

    }
}
