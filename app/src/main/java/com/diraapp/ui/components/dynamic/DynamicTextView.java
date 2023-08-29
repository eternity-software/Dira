package com.diraapp.ui.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.annotation.StyleableRes;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.Theme;
import com.diraapp.res.lang.CustomLanguage;
import com.diraapp.ui.components.TightTextView;


public class DynamicTextView extends TightTextView {

    @StyleableRes
    int localizableKey = 0;
    @StyleableRes
    int themeColor = 0;
    CharSequence backgroundTint, textLinkColor, textColor, locId;
    private CharSequence localId;

    public DynamicTextView(Context context) {
        super(context);
    }

    public DynamicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }


    public DynamicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        setupTheme();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        setupTheme();
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