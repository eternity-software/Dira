package com.diraapp.ui.activities;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.storage.images.WaterfallBalancer;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for activities with utility methods
 * <p>
 * ⢀⡴⠑⡄⠀⠀⠀⠀⠀⠀⠀⣀⣀⣤⣤⣤⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠸⡇⠀⠿⡀⠀⠀⠀⣀⡴⢿⣿⣿⣿⣿⣿⣿⣿⣷⣦⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠑⢄⣠⠾⠁⣀⣄⡈⠙⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⢀⡀⠁⠀⠀⠈⠙⠛⠂⠈⣿⣿⣿⣿⣿⠿⡿⢿⣆⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⢀⡾⣁⣀⠀⠴⠂⠙⣗⡀⠀⢻⣿⣿⠭⢤⣴⣦⣤⣹⠀⠀⠀⢀⢴⣶⣆
 * ⠀⠀⢀⣾⣿⣿⣿⣷⣮⣽⣾⣿⣥⣴⣿⣿⡿⢂⠔⢚⡿⢿⣿⣦⣴⣾⠁⠸⣼⡿
 * ⠀⢀⡞⠁⠙⠻⠿⠟⠉⠀⠛⢹⣿⣿⣿⣿⣿⣌⢤⣼⣿⣾⣿⡟⠉⠀⠀⠀⠀⠀
 * ⠀⣾⣷⣶⠇⠀⠀⣤⣄⣀⡀⠈⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀
 * ⠀⠉⠈⠉⠀⠀⢦⡈⢻⣿⣿⣿⣶⣶⣶⣶⣤⣽⡹⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠉⠲⣽⡻⢿⣿⣿⣿⣿⣿⣿⣷⣜⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣷⣶⣮⣭⣽⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⣀⣀⣈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠇⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀
 * ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠻⠿⠿⠿⠿⠛⠉
 */
public class DiraActivity extends AppCompatActivity {

    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(8);
    private final List<DiraActivityListener> activityListenerList = new ArrayList<>();
    private WaterfallBalancer waterfallBalancer;
    private CacheUtils cacheUtils;


    public static void runGlobalBackground(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static Bitmap captureView(View view) {
        Bitmap tBitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tBitmap);
        view.draw(canvas);
        canvas.setBitmap(null);
        return tBitmap;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_enter_anim, R.anim.activity_enter_anim);
        waterfallBalancer = new WaterfallBalancer(this);
        for (DiraActivityListener listener : activityListenerList) listener.onCreate();
    }

    public void runBackground(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    public CacheUtils getCacheUtils() {
        if (cacheUtils == null) cacheUtils = new CacheUtils(getApplicationContext());
        return cacheUtils;
    }

    public WaterfallBalancer getWaterfallBalancer() {
        return waterfallBalancer;
    }

    public ScaleAnimation performScaleAnimation(float fromScale, float toScale, View view) {
        ScaleAnimation scaleOut = new ScaleAnimation(fromScale, toScale,
                fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleOut.setDuration(200);
        scaleOut.setInterpolator(new DecelerateInterpolator(2f));


        scaleOut.setFillAfter(true);

        view.startAnimation(scaleOut);
        return scaleOut;
    }

    public ValueAnimator performHeightAnimation(int fromHeight, int toHeight, View view) {
        ValueAnimator animator = ValueAnimator.ofInt(fromHeight, toHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = value;
                view.setLayoutParams(params);
            }
        });
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.setDuration(150);
        animator.start();
        return animator;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_exit_anim);
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (DiraActivityListener listener : new ArrayList<>(activityListenerList))
            listener.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (DiraActivityListener listener : new ArrayList<>(activityListenerList))
            listener.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (DiraActivityListener listener : new ArrayList<>(activityListenerList))
            listener.onResume();
    }

    public void addListener(DiraActivityListener diraActivityListener) {
        activityListenerList.add(diraActivityListener);
    }

    public void removeListener(DiraActivityListener diraActivityListener) {
        activityListenerList.remove(diraActivityListener);
    }

}
