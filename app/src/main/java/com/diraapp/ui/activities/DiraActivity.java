package com.diraapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
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

    private static final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(4);
    private final List<DiraActivityListener> activityListenerList = new ArrayList<>();
    private CacheUtils cacheUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_enter_anim, R.anim.activity_enter_anim);
        for (DiraActivityListener listener : activityListenerList) listener.onCreate();
    }

    public static void runGlobalBackground(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    public void runBackground(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    public CacheUtils getCacheUtils() {
        if (cacheUtils == null) cacheUtils = new CacheUtils(getApplicationContext());
        return cacheUtils;
    }

    public ScaleAnimation preformScaleAnimation(float fromScale, float toScale, View view) {
        ScaleAnimation scaleOut = new ScaleAnimation(fromScale, toScale,
                fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleOut.setDuration(200);
        scaleOut.setInterpolator(new DecelerateInterpolator(2f));


        scaleOut.setFillAfter(true);

        view.startAnimation(scaleOut);
        return scaleOut;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_exit_anim, R.anim.activity_exit_anim);
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (DiraActivityListener listener : activityListenerList) listener.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (DiraActivityListener listener : activityListenerList) listener.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (DiraActivityListener listener : activityListenerList) listener.onResume();
    }

    public void addListener(DiraActivityListener diraActivityListener) {
        activityListenerList.add(diraActivityListener);
    }

    public void removeListener(DiraActivityListener diraActivityListener) {
        activityListenerList.remove(diraActivityListener);
    }

}
