package com.diraapp.storage.images;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.diraapp.ui.bottomsheet.filepicker.FileInfo;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.utils.ImageRotationFix;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;


public class WaterfallImageLoader {

    private final List<FilePreview> imagesQueue = new ArrayList<>();
    private final Activity activity;
    private boolean isRunning;
    private boolean isDataUpdated;
    private WaterfallCallback waterfallCallback;

    public WaterfallImageLoader(Activity activity) {
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

                            WaterfallLogger.log("Starting worker...");
                            List<FilePreview> localList = (new ArrayList<>());
                            localList.addAll(imagesQueue);
                            for (final FilePreview imageView : localList) {
                                final boolean[] isCancelled = {false};
                                WaterfallLogger.log("Loading " + imageView.getFileInfo());

                                try {
                                    final Bitmap bitmap;
                                    if (imageView.getFileInfo().isImage()) {
                                        bitmap = decodeFile(new File(imageView.getFileInfo().getFilePath()));
                                    } else {
                                        bitmap = imageView.getFileInfo().getVideoThumbnail();
                                    }


                                    if (bitmap != null) {
                                        final Bitmap fixedBitmap;
                                        final String oldUri = imageView.getFileInfo().getFilePath();
                                        if (imageView.getFileInfo().isImage()) {
                                            fixedBitmap = ImageRotationFix.handleSamplingAndRotationBitmap(activity, Uri.fromFile(new File(imageView.getFileInfo().getFilePath())));
                                        } else {
                                            fixedBitmap = bitmap;
                                        }
                                        final String subtitle;
                                        if (imageView.getFileInfo().isVideo()) {
                                            subtitle = FileInfo.getFormattedVideoDuration(activity, imageView.getFileInfo().getFilePath());
                                        } else {
                                            subtitle = "";
                                        }
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {


                                                //  Glide.with(activity).load(imageView.getImagePath()).into(imageView);
                                                // Old trivial way

                                                try {


                                                    if (imageView.getFileInfo().getFilePath().equals(oldUri)) {
                                                        imageView.getFileParingImageView().setImageBitmap(fixedBitmap);
                                                        imageView.setSubtitle(subtitle);

                                                        if (waterfallCallback != null) {
                                                            waterfallCallback.onImageProcessedSuccess(imageView);
                                                        }
                                                        Animation fadeIn = new AlphaAnimation(0, 1);
                                                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                                                        fadeIn.setDuration(400);

                                                        imageView.getFileParingImageView().startAnimation(fadeIn);
                                                    } else {


                                                        if (WaterfallBalancer.DEBUG_MODE) {
                                                            imageView.setBackgroundColor(Color.MAGENTA);
                                                            //  imageView.getFileParingImageView().setImageBitmap(null);
                                                        }
                                                        waterfallCallback.onImageReplaced(imageView);
                                                    }
                                                } catch (Exception e) {
                                                    if (waterfallCallback != null) {
                                                        waterfallCallback.onImageProcessedError(imageView);
                                                    }
                                                    e.printStackTrace();
                                                }


                                            }
                                        });
                                    } else {
                                        if (waterfallCallback != null) {
                                            waterfallCallback.onImageProcessedError(imageView);
                                        }
                                    }
                                } catch (final Exception e) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (waterfallCallback != null) {
                                                waterfallCallback.onImageProcessedError(imageView);
                                            }

                                            if (WaterfallBalancer.DEBUG_MODE) {
                                                imageView.setBackgroundColor(Color.RED);

                                            }

                                        }
                                    });
                                    e.printStackTrace();

                                }

                                imagesQueue.remove(imageView);


                            }

                            if (!isDataUpdated) {
                                WaterfallLogger.log("Data update isn't detected");
                                isRunning = false;
                                waterfallCallback.onFinishedAllTasks();
                            } else {
                                isDataUpdated = false;
                                WaterfallLogger.log("Updated data detected!");
                            }

                        } catch (ConcurrentModificationException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                    WaterfallLogger.log("Waterfall's worker done!");
                }
            });
            worker.start();
        }
    }

    public void add(FilePreview imageView) {
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

        int IMAGE_MAX_SIZE = Math.max(MAX_SIZE, MAX_SIZE);
        if (o.outHeight > MAX_SIZE || o.outWidth > MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
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
        void onImageProcessedSuccess(FilePreview filePreview);

        void onImageProcessedError(FilePreview filePreview);

        void onImageReplaced(FilePreview filePreview);

        void onFinishedAllTasks();

        void onStarted();

    }

    private static class WaterfallLogger {
        public static void log(String message) {
            System.out.println("WATERFALL LOADER >> " + message);
        }
    }


}
