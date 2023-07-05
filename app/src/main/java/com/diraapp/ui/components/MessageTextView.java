package com.diraapp.ui.components;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;


public class MessageTextView extends androidx.appcompat.widget.AppCompatTextView {


    public MessageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int widthMode = MeasureSpec.getMode(widthSpec);


        Layout layout = getLayout();
        if (layout != null) {
            int maxWidth = (int) Math.ceil(getMaxLineWidth2(layout));
            widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthSpec, heightSpec);
    }


    public void initializeSize() {
        try {
            int width = (int) getMaxLineWidth(getLayout());
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);

            setWidth(width);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private float getMaxLineWidth2(Layout layout) {
        float maximumWidth = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            maximumWidth = Math.max(layout.getLineWidth(i), maximumWidth);
        }

        return maximumWidth;
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }
}