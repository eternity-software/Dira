package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;
import com.google.android.material.imageview.ShapeableImageView;

/**
 * ImageView with rounded corners and that can be
 * associated with many file types (as image, video etc)
 *
 * @author Mikhail Karlov
 */
public class FileParingImageView extends ShapeableImageView {

    private SelectorFileInfo selectorFileInfo;
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

    public SelectorFileInfo getFileInfo() {
        return selectorFileInfo;
    }

    public void setFileInfo(SelectorFileInfo filePath) {
        this.selectorFileInfo = filePath;
    }
}
