package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DiraMediaInfo;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * ImageView that can be
 * associated with many file types (as image, video etc)
 *
 * @author Mikhail Karlov
 */
public class FileParingImageView extends androidx.appcompat.widget.AppCompatImageView {

    private DiraMediaInfo diraMediaInfo;
    private Bitmap bitmap;

    public FileParingImageView(Context context) {
        super(context);
    }

    public FileParingImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public FileParingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmap = bm;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public DiraMediaInfo getFileInfo() {
        return diraMediaInfo;
    }

    public void setFileInfo(DiraMediaInfo filePath) {
        this.diraMediaInfo = filePath;
    }
}
