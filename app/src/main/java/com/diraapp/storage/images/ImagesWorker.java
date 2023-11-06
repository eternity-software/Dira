package com.diraapp.storage.images;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;


public class ImagesWorker {
    private static final long MAX_SIZE_BYTES = 800000;

    public static void saveBitmapToGallery(Bitmap finalBitmap, Activity activity) {
        String fname = "DiraSaved-" + System.currentTimeMillis() + ".jpg";
        MediaStore.Images.Media.insertImage(activity.getContentResolver(), finalBitmap, fname, "DiraApp");
    }

    public static Bitmap centerCropBitmap(Bitmap sourceBitmap)
    {
        Bitmap croppedBitmap;
        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()){

            croppedBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    sourceBitmap.getWidth()/2 - sourceBitmap.getHeight()/2,
                    0,
                    sourceBitmap.getHeight(),
                    sourceBitmap.getHeight()
            );

        }else{

            croppedBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    sourceBitmap.getHeight()/2 - sourceBitmap.getWidth()/2,
                    sourceBitmap.getWidth(),
                    sourceBitmap.getWidth()
            );
        }
        if(sourceBitmap != croppedBitmap) sourceBitmap.recycle();
        return croppedBitmap;
    }

    public static Bitmap getCircleCroppedBitmap(Bitmap bitmap, int height, int width) {
        Bitmap output;
        if (bitmap == null) return null;
        bitmap = centerCropBitmap(bitmap);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            }
        }
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        int widthLight = bitmap.getWidth();
        int heightLight = bitmap.getHeight();

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

        canvas.drawRoundRect(rectF, widthLight / 2, heightLight / 2, paintColor);

        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0, 0, paintImage);

        return output;
    }


    public static Bitmap resizeBitmap(Bitmap bitmap, int maxFrameSize) {

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if (bitmap.getHeight() * bitmap.getWidth() > maxFrameSize * maxFrameSize) {

            float scaleFactor = (bitmap.getHeight() * bitmap.getWidth()) / (float) (maxFrameSize * maxFrameSize);

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width / scaleFactor),
                    (int) (height / scaleFactor), true);
        }

        return bitmap;
    }


    public static Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int currSize;
        int currQuality = 50;

        bitmap.compress(Bitmap.CompressFormat.JPEG, currQuality, stream);
        currSize = stream.toByteArray().length;

        while (currSize > MAX_SIZE_BYTES && currQuality > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.PNG, currQuality, stream);
            }
            currSize = stream.toByteArray().length;
            currQuality -= 5;
        }

        return BitmapFactory.decodeByteArray(stream.toByteArray(), 0, currSize);
    }


}
