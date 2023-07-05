package com.diraapp.ui.components;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;


public class LeftMessageTextView extends androidx.appcompat.widget.AppCompatTextView {

    public LeftMessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getLayout() != null) {
            int width = (int) Math.ceil(getMaxLineWidth(getLayout()));
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);
            super.onMeasure(width, heightMeasureSpec);

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private float getMaxLineWidth(Layout layout) {
        float maxWidth = 0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > maxWidth) {
                maxWidth = layout.getLineWidth(i);
            }
        }
        return maxWidth;
    }
}