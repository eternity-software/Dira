package ru.dira.attachments;

import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.dira.components.FilePreview;


public class WaterfallBalancer {


    public static final boolean DEBUG_MODE = false;

    List<WaterfallImageLoader> waterfallImageLoaderList = new ArrayList<>();
    ArrayList<FilePreview> imagesInTask = new ArrayList<>();
    ArrayList<FilePreview> cancelledList = new ArrayList<>();
    private int lastWaterfallId;
    private int activeWaterfalls = 0;
    private TextView debugText;
    private BalancerCallback balancerCallback;


    public static interface BalancerCallback
    {
        void onActiveWaterfallsCountChange(int count);
    }

    public void setBalancerCallback(BalancerCallback balancerCallback) {
        this.balancerCallback = balancerCallback;
    }

    public WaterfallBalancer(Activity activity, int balancerCount, final RecyclerView recyclerView)
    {
        lastWaterfallId = 0;
        for(int i = 0; i < balancerCount; i++)
        {
            WaterfallImageLoader waterfallImageLoader = new WaterfallImageLoader(activity);
            waterfallImageLoader.setWaterfallCallback(new WaterfallImageLoader.WaterfallCallback() {
                @Override
                public void onImageProcessedSuccess(FilePreview fileParingImageView) {
                    imagesInTask.remove(fileParingImageView);
                    if(DEBUG_MODE)
                    {
                        fileParingImageView.setBackgroundColor(Color.GREEN);
                    }

                }

                @Override
                public void onImageProcessedError(FilePreview fileParingImageView) {
                    imagesInTask.remove(fileParingImageView);
                    if(DEBUG_MODE)
                    {
                        fileParingImageView.setBackgroundColor(Color.RED);
                    }
                }

                @Override
                public void onImageReplaced(FilePreview fileParingImageView) {

                    imagesInTask.remove(fileParingImageView);
                    add(fileParingImageView);
                }

                @Override
                public void onFinishedAllTasks() {
                    activeWaterfalls -= 1;
                    if(activeWaterfalls == 0) clearCancelled();
                    if(balancerCallback != null)
                    {
                        balancerCallback.onActiveWaterfallsCountChange(activeWaterfalls);
                    }
                }

                @Override
                public void onStarted() {
                    activeWaterfalls++;

                    if(balancerCallback != null)
                    {
                        balancerCallback.onActiveWaterfallsCountChange(activeWaterfalls);
                    }
                }


            });
            waterfallImageLoaderList.add(waterfallImageLoader);
        }
    }

    public void clearCancelled()
    {
        for(FilePreview cancelled : cancelledList)
        {
           // imagesInTask.remove(cancelled);
        }
    }

    public void add(FilePreview fileParingImageView) {


        if (imagesInTask.contains(fileParingImageView)) {
            cancelledList.add(fileParingImageView);
            if (DEBUG_MODE) {
                fileParingImageView.setBackgroundColor(Color.DKGRAY);
            }
        }
        else
        {
            if (DEBUG_MODE) {
                fileParingImageView.setBackgroundColor(Color.YELLOW);
            }
            if (lastWaterfallId > waterfallImageLoaderList.size() - 1) {
                lastWaterfallId = 0;
            }
            waterfallImageLoaderList.get(lastWaterfallId).add(fileParingImageView);
            imagesInTask.add(fileParingImageView);
            lastWaterfallId++;

        }




    }
}
