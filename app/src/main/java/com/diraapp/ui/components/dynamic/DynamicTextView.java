package com.diraapp.ui.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.annotation.StyleableRes;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.Theme;
import com.diraapp.res.lang.CustomLanguage;


public class DynamicTextView extends androidx.appcompat.widget.AppCompatTextView {

    @StyleableRes
    int localizableKey = 0;
    @StyleableRes
    int themeColor = 0;
    CharSequence backgroundTint, textLinkColor, textColor, locId;
    private CharSequence localId;
    private boolean isInit = false;

    public DynamicTextView(Context context) {
        super(context);
    }

    public DynamicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = getLayout();
        if (layout != null) {
            int width = (int) Math.ceil(getMaxLineWidth(layout))
                    + getCompoundPaddingLeft() + getCompoundPaddingRight();
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
    }

    private float getMaxLineWidth(Layout layout) {
        float max_width = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineWidth(i);
            }
        }
        return max_width;
    }
    public DynamicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    public void setupTheme() {
        try {
            setTextColor(Theme.getColor(String.valueOf(textColor)));
        } catch (NoSuchValueException e) {
            setTextColor(Theme.getColor(getContext(), Theme.getResId(String.valueOf(textColor), R.color.class)));
        }

        try {
            setLinkTextColor(Theme.getColor(String.valueOf(textLinkColor)));
        } catch (NoSuchValueException e) {
            setLinkTextColor(Theme.getColor(getContext(), Theme.getResId(String.valueOf(textLinkColor), R.color.class)));
        }
        try {
            setText(CustomLanguage.getStringsRepository().getValue(String.valueOf(locId)));
        } catch (NoSuchValueException ignored) {

        }

        if (getBackground() != null) {
            try {
                getBackground().setColorFilter(null);
                getBackground().setColorFilter(Theme.getColor(String.valueOf(backgroundTint)), PorterDuff.Mode.SRC_ATOP);
            } catch (NoSuchValueException e) {

                if (getBackground() != null)
                    getBackground().setColorFilter(Theme.getColor(getContext(), Theme.getResId(String.valueOf(backgroundTint), R.color.class)), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @SuppressLint("ResourceType")
    private void initialize(Context context, AttributeSet attrs) {
        if (isInit) return;
        isInit = true;
        int[] sets = {R.attr.localizableKey, R.attr.themeColor, R.attr.themeColorBackground, R.attr.themeColorLink};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);
        locId = typedArray.getText(0);
        textColor = typedArray.getText(1);
        textLinkColor = typedArray.getText(3);
        backgroundTint = typedArray.getText(2);
        setupTheme();
        localId = locId;

        typedArray.recycle();
    }


}