package com.diraapp.ui.components;

import static com.diraapp.storage.AppStorage.DIRA_FILES_PATH;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.diraapp.R;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.media.SoundRecorder;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DiraVibrator;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Mode;

import java.io.File;

/**
 * Controller for recording bubbles and voice messages
 * Provides animations and requires specific UI components
 */
public class RecordComponentsController {

    private static final String TAG = "MediaMessageRecorder";
    private final ImageView recordButton;
    private final ImageView recordRipple;
    private final DiraActivity context;
    private final SoundRecorder soundRecorder;
    private final CameraView camera;
    private final View recordBubbleLayout;
    private final CardView bubbleContainer;
    private long lastTimeRecordButtonDown = 0;
    private long lastTimeRecordButtonUp = 0;
    private boolean isRecordButtonVisible = true;

    private RecordListener recordListener;
    private float lastScale = 1;

    public RecordComponentsController(ImageView recordButton,
                                      ImageView recordRipple,
                                      DiraActivity context,
                                      View recordBubbleLayout,
                                      CardView bubbleContainer) {
        this.recordButton = recordButton;
        this.recordRipple = recordRipple;
        this.context = context;
        this.camera = new CameraView(context);

        this.camera.setFacing(Facing.FRONT);
        this.camera.setKeepScreenOn(true);

        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        camera.setLayoutParams(params);

        bubbleContainer.addView(camera);
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
                if (recordListener != null) {
                    recordListener.onMediaMessageRecorded(result.getFile().getPath(), AttachmentType.BUBBLE);
                    camera.close();
                    camera.setLifecycleOwner(null);
                    camera.setEnabled(false);
                }
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
                            CacheUtils cacheUtils = context.getCacheUtils();
                            boolean isVoiceRecord = cacheUtils.getBoolean(CacheUtils.IS_VOICE_RECORD_DEFAULT);
                            if (!isVoiceRecord) {
                                recordBubble();
                                DiraVibrator.vibrateOneTime(context);
                            }
                            else
                            {
                                recordListener.onMediaMessageRecordingStart(AttachmentType.VOICE);
                                initVoiceIndicator();
                            }


                        }
                    }, 200);
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

                            camera.close();
                            camera.setEnabled(false);

                            recordBubbleLayout.setVisibility(View.GONE);
                        } else {
                            if (recordListener != null) {
                                recordListener.onMediaMessageRecorded(soundRecorder.getVoiceMessagePath(),
                                        AttachmentType.VOICE);
                            }

                        }

                        context.performScaleAnimation(lastScale, 0, recordRipple);
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
        context.performScaleAnimation(0.5f, 1, bubbleContainer).setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                camera.setLifecycleOwner(context);


                camera.addCameraListener(new CameraListener() {
                    @Override
                    public void onCameraOpened(@NonNull CameraOptions options) {
                        super.onCameraOpened(options);
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Taking captured video...");
                        camera.takeVideoSnapshot(new File(directory, "bubbleMessage.mp4"));
                        camera.removeCameraListener(this);
                    }
                });
                camera.open();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

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


        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    1);
            return;
        }

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
