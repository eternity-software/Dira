package ru.dira.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.util.ViewDragHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.dira.R;

public class SliderActivity {

    /**
     * Keyboard transparent issue
     * Code from https://github.com/r0adkll/Slidr/issues/72
     */

    private float percent = 0;

    private final List<SliderChangeListener> sliderChangeListenerList = new ArrayList<>();

    public static void setAppTheme(Activity activity, int state) {


        if (state == ViewDragHelper.STATE_SETTLING) {
            return;
        }

        if (state == ViewDragHelper.STATE_DRAGGING) {
            if (activity.getWindow().getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity.getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        final @ColorRes int color = getWindowColor(state);
        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(activity, color));
        //  activity.getWindow().setBackgroundDrawable(colorDrawable);
        activity.getWindow().getDecorView().setBackgroundColor(activity.getResources().getColor(color));

    }

    @ColorRes
    private static int getWindowColor(int state) {
        switch (state) {
            case ViewDragHelper.STATE_IDLE:
                return R.color.dark;
            case ViewDragHelper.STATE_DRAGGING:
                return android.R.color.transparent;
            default:
                String errorMessage = String.format(Locale.getDefault(), "Cannot resolve WindowColor for state: %d", state);
                throw new IllegalArgumentException(errorMessage);
        }
    }

    public void addListener(SliderChangeListener sliderChangeListener) {
        sliderChangeListenerList.add(sliderChangeListener);
    }

    public void removeListener(SliderChangeListener sliderChangeListener) {
        sliderChangeListenerList.remove(sliderChangeListener);
    }

    public SlidrInterface attachSlider(final Activity activity) {
        try {
            setAppTheme(activity, ViewDragHelper.STATE_IDLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final SlidrConfig slidrConfig = new SlidrConfig.Builder().listener(new SlidrListener() {

            @Override
            public void onSlideStateChanged(int state) {

                for (SliderChangeListener sliderChangeListener : sliderChangeListenerList) {
                    sliderChangeListener.onDragging(state);
                }
                setAppTheme(activity, state);
            }

            @Override
            public void onSlideChange(float percent) {
                SliderActivity.this.percent = percent;

            }

            @Override
            public void onSlideOpened() {
            }

            @Override
            public boolean onSlideClosed() {

                return false;
            }
        }).build();

        return Slidr.attach(activity, slidrConfig);
    }


    public interface SliderChangeListener {
        void onDragging(int state);
    }
}
