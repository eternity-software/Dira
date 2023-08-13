package com.diraapp.ui.activities;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.utils.CacheUtils;

/**
 * Base class for activities with utility methods
 */
public class DiraActivity extends AppCompatActivity {

    private CacheUtils cacheUtils;


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

}
