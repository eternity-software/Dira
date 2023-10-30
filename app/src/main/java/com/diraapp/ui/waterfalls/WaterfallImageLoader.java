package com.diraapp.ui.waterfalls;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Display;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DiraMediaInfo;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.components.ImagePreview;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.ui.components.WaterfallImageView;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;


public class WaterfallImageLoader {

    private final List<WaterfallImageView> imagesQueue = new ArrayList<>();
    private final DiraActivity activity;
    private boolean isRunning;
    private boolean isDataUpdated;
    private WaterfallCallback waterfallCallback;

    public WaterfallImageLoader(DiraActivity activity) {
        this.activity = activity;
        isRunning = false;
        isDataUpdated = false;
    }

    public void setWaterfallCallback(WaterfallCallback waterfallCallback) {
        this.waterfallCallback = waterfallCallback;
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            waterfallCallback.onStarted();
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {

                            //WaterfallLogger.log("Starting worker...");
                            List<WaterfallImageView> localList = (new ArrayList<>());
                            localList.addAll(imagesQueue);
                            for (final WaterfallImageView imageView : localList) {
                                final boolean[] isCancelled = {false};

                                try {
                                    if (imageView != null) {
                                        //WaterfallLogger.log("Loading " + imageView.getFileInfo());
                                        DiraMediaInfo oldFileInfo = imageView.getFileInfo();
                                        Bitmap bitmap;
                                        if (imageView.getFileInfo().isImage()) {
                                            if (imageView instanceof ImagePreview) {
                                                Bitmap oldBitmap = AppStorage.getBitmapFromPath(imageView.getFileInfo().getFilePath(), activity);


                                                float scale = (float) Resources.getSystem().getDisplayMetrics().widthPixels * 0.5f /  oldBitmap.getWidth();

                                                if (scale >= 1) {
                                                    bitmap = oldBitmap;
                                                } else {
                                                    bitmap = Bitmap.createScaledBitmap(oldBitmap, (int) (oldBitmap.getWidth() * scale),
                                                            (int) (oldBitmap.getHeight() * scale), true);

                                                    oldBitmap.recycle();
                                                    oldBitmap = null;
                                                }

                                            } else {
                                                bitmap = decodeFile(new File(imageView.getFileInfo().getFilePath()));
                                            }
                                        } else {
                                            bitmap = imageView.getFileInfo().getVideoThumbnail();
                                        }




                                        if (bitmap != null) {

                                            final String oldUri = imageView.getFileInfo().getFilePath();

                                            final String subtitle;
                                            if (imageView.getFileInfo().isVideo()) {
                                                subtitle = DiraMediaInfo.getFormattedVideoDuration(activity, imageView.getFileInfo().getFilePath());
                                            } else {
                                                subtitle = "";
                                            }

                                            Bitmap finalBitmap = bitmap;
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (!oldFileInfo.getFilePath().equals(imageView.getFileInfo().getFilePath())) {
                                                        finalBitmap.recycle();
                                                        add(imageView);
                                                        return;
                                                    }
                                                    //  Glide.with(activity).load(imageView.getImagePath()).into(imageView);
                                                    // Old trivial way

                                                    try {


                                                        if (imageView.getFileInfo().getFilePath().equals(oldUri)) {
                                                            if (imageView instanceof MediaGridItem) {
                                                                MediaGridItem mediaGridItem = (MediaGridItem) imageView;

                                                                mediaGridItem.setSubtitle(subtitle);
                                                            }
                                                            if (imageView instanceof ImagePreview)
                                                            {
                                                                ((ImagePreview) imageView).setImageBitmap(finalBitmap);
                                                            }
                                                            else
                                                            {
                                                                imageView.getImageView().setImageBitmap(finalBitmap);
                                                            }


                                                            Animation fadeIn = new AlphaAnimation(0, 1);
                                                            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                                                            fadeIn.setDuration(200);


                                                            if (imageView instanceof MediaGridItem) {

                                                                activity.performScaleAnimation(0.5f, 1, (View) imageView);
                                                            }


                                                            imageView.getImageView().startAnimation(fadeIn);
                                                            if (waterfallCallback != null) {
                                                                imageView.onImageBind(finalBitmap);
                                                                waterfallCallback.onImageProcessedSuccess(imageView);
                                                            }
                                                        } else {


                                                            WaterfallBalancer.setDebugColor(Color.MAGENTA, imageView);
                                                            waterfallCallback.onImageReplaced(imageView);
                                                        }
                                                    } catch (Exception e) {
                                                        finalBitmap.recycle();
                                                        if (waterfallCallback != null) {
                                                            waterfallCallback.onImageProcessedError(imageView);
                                                        }
                                                        e.printStackTrace();
                                                    }



                                                }
                                            });

                                        } else {
                                            //WaterfallLogger.log("bitmap null " + imageView.getFileInfo().isImage());
                                            if (waterfallCallback != null) {
                                                waterfallCallback.onImageProcessedError(imageView);
                                            }
                                        }
                                    }
                                } catch (final Exception e) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (waterfallCallback != null) {
                                                waterfallCallback.onImageProcessedError(imageView);
                                            }
                                            WaterfallBalancer.setDebugColor(Color.RED, imageView);

                                        }
                                    });
                                    e.printStackTrace();

                                }

                                imagesQueue.remove(imageView);


                            }

                            if (!isDataUpdated) {
                                //WaterfallLogger.log("Data update isn't detected");
                                isRunning = false;
                                waterfallCallback.onFinishedAllTasks();
                            } else {
                                isDataUpdated = false;
                                //WaterfallLogger.log("Updated data detected!");
                            }

                        } catch (ConcurrentModificationException ignored) {
                            ignored.printStackTrace();
                        }
                    }

                    //WaterfallLogger.log("Waterfall's worker done!");
                }
            });
            worker.start();
        }
    }

    public void reset()
    {
        imagesQueue.clear();
    }

    public void add(WaterfallImageView imageView) {
        if (!(imageView instanceof View)) throw new RuntimeException("Must extend View");
        imagesQueue.add(imageView);
        if (isRunning) {
            isDataUpdated = true;
        } else {
            start();
        }
    }

    private Bitmap decodeFile(File f) throws Exception {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;

        int MAX_SIZE = 500;

        int IMAGE_MAX_SIZE = MAX_SIZE;
        if (o.outHeight > MAX_SIZE || o.outWidth > MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = 2;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return b;
    }

    private boolean isVisible(final View view) {
        if (view == null) {
            return false;
        }
        return view.isShown();
    }

    public interface WaterfallCallback {
        void onImageProcessedSuccess(WaterfallImageView filePreview);

        void onImageProcessedError(WaterfallImageView filePreview);

        void onImageReplaced(WaterfallImageView filePreview);

        void onFinishedAllTasks();

        void onStarted();

    }

    private static class WaterfallLogger {
        public static void log(String message) {
            Logger.logDebug("Waterfall",
                    message);
        }
    }


}
