package com.diraapp.storage.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.BuildConfig;
import com.diraapp.device.PerformanceClass;
import com.diraapp.device.PerformanceTester;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.ui.components.WaterfallImageView;

import java.util.ArrayList;
import java.util.List;


public class WaterfallBalancer {


    public static final boolean DEBUG_MODE = BuildConfig.DEBUG;

    List<WaterfallImageLoader> waterfallImageLoaderList = new ArrayList<>();
    ArrayList<WaterfallImageView> imagesInTask = new ArrayList<>();
    ArrayList<WaterfallImageView> cancelledList = new ArrayList<>();
    private int lastWaterfallId;
    private int activeWaterfalls = 0;
    private TextView debugText;
    private BalancerCallback balancerCallback;


    public WaterfallBalancer(DiraActivity diraActivity)
    {
         this(diraActivity, getHardwareDependBalancerCount(diraActivity));
    }

    public WaterfallBalancer(DiraActivity activity, int balancerCount) {
        lastWaterfallId = 0;
        for (int i = 0; i < balancerCount; i++) {
            WaterfallImageLoader waterfallImageLoader = new WaterfallImageLoader(activity);
            waterfallImageLoader.setWaterfallCallback(new WaterfallImageLoader.WaterfallCallback() {
                @Override
                public void onImageProcessedSuccess(WaterfallImageView fileParingImageView) {
                    imagesInTask.remove(fileParingImageView);
                    setDebugColor(Color.GREEN, fileParingImageView);

                }

                @Override
                public void onImageProcessedError(WaterfallImageView fileParingImageView) {

                    imagesInTask.remove(fileParingImageView);
                    setDebugColor(Color.RED, fileParingImageView);
                }

                @Override
                public void onImageReplaced(WaterfallImageView fileParingImageView) {

                    imagesInTask.remove(fileParingImageView);
                    add(fileParingImageView);
                }

                @Override
                public void onFinishedAllTasks() {
                    activeWaterfalls -= 1;
                    if (activeWaterfalls == 0) clearCancelled();
                    if (balancerCallback != null) {
                        balancerCallback.onActiveWaterfallsCountChange(activeWaterfalls);
                    }
                }

                @Override
                public void onStarted() {
                    activeWaterfalls++;

                    if (balancerCallback != null) {
                        balancerCallback.onActiveWaterfallsCountChange(activeWaterfalls);
                    }
                }


            });
            waterfallImageLoaderList.add(waterfallImageLoader);
        }
    }



    public void setBalancerCallback(BalancerCallback balancerCallback) {
        this.balancerCallback = balancerCallback;
    }

    public void clearCancelled() {
      /*  for (FilePreview cancelled : cancelledList) {
            // imagesInTask.remove(cancelled);
        }*/
    }

    public void add(WaterfallImageView fileParingImageView) {


        if (imagesInTask.contains(fileParingImageView)) {
            cancelledList.add(fileParingImageView);
            setDebugColor(Color.DKGRAY, fileParingImageView);
        } else {

                setDebugColor(Color.YELLOW, fileParingImageView);
            if (lastWaterfallId > waterfallImageLoaderList.size() - 1) {
                lastWaterfallId = 0;
            }
            waterfallImageLoaderList.get(lastWaterfallId).add(fileParingImageView);
            imagesInTask.add(fileParingImageView);
            lastWaterfallId++;

        }


    }


    public static void setDebugColor(int color, WaterfallImageView waterfallImageView)
    {
        if(!DEBUG_MODE) return;
        if(waterfallImageView instanceof View)
        {
            DiraActivity.runOnMainThread(() -> {
                ((View) waterfallImageView).setBackgroundColor(color);
            });
        }
    }
    public void remove(FilePreview filePreview)
    {
       // cancelledList.add(filePreview);
    }

    private static int getHardwareDependBalancerCount(Context context) {
        int balancerCount = 2;

        PerformanceClass performanceClass = PerformanceTester.measureDevicePerformanceClass(context);
        if (performanceClass == PerformanceClass.MEDIUM) {
            balancerCount = 8;
        } else if (performanceClass == PerformanceClass.HIGH) {
            balancerCount = 14;
        }
        return balancerCount;
    }

    public interface BalancerCallback {
        void onActiveWaterfallsCountChange(int count);
    }
}
