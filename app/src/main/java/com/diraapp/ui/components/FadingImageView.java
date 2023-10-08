package com.diraapp.ui.components;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;

public class FadingImageView extends androidx.appcompat.widget.AppCompatImageView {
    private final Context c;
    private boolean mFadeRight;
    private boolean mFadeLeft;
    private boolean mFadeTop;
    private boolean mFadeBottom;


    public FadingImageView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);

        this.c = c;

        init();
    }

    public FadingImageView(Context c, AttributeSet attrs) {
        super(c, attrs);

        this.c = c;

        init();
    }

    public FadingImageView(Context c) {
        super(c);

        this.c = c;

        init();
    }

    private void init() {
        // Enable horizontal fading
        this.setHorizontalFadingEdgeEnabled(true);
        this.setVerticalFadingEdgeEnabled(true);
        // Apply default fading length
        this.setEdgeLength(14);
        // Apply default side
        this.setFadeRight(true);
    }


    public void setFadeRight(boolean fadeRight) {
        mFadeRight = fadeRight;
    }


    public void setFadeLeft(boolean fadeLeft) {
        mFadeLeft = fadeLeft;
    }


    public void setFadeTop(boolean fadeTop) {
        mFadeTop = fadeTop;
    }

    public void setFadeBottom(boolean fadeBottom) {
        mFadeBottom = fadeBottom;
    }

    public void setEdgeLength(int length) {
        this.setFadingEdgeLength(getPixels(length));
    }


    @Override
    protected float getTopFadingEdgeStrength() {
        return mFadeTop ? 5.0f : 0.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return mFadeBottom ? 5.0f : 0.0f;
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        return mFadeLeft ? 5.0f : 0.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        return mFadeRight ? 1.0f : 0.0f;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return true;
    }

    @Override
    public boolean onSetAlpha(int alpha) {
        return false;
    }

    private int getPixels(int dipValue) {
        Resources r = c.getResources();

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dipValue, r.getDisplayMetrics());
    }
}