package com.diraapp.ui.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.Theme;

public class ThemeImageView extends androidx.appcompat.widget.AppCompatImageView {


    private final Boolean isPreviewLoading = false;
    private CharSequence localId;

    public ThemeImageView(Context context) {
        super(context);
    }

    public ThemeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ThemeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @SuppressLint("ResourceType")
    private void initialize(Context context, AttributeSet attrs) {
        TypedArray typedArray = null;
        try {
            int[] sets = {R.attr.themeBackgroundTint, R.attr.themeImageColor};
            typedArray = context.obtainStyledAttributes(attrs, sets);
            String backgroundTint = String.valueOf(typedArray.getText(0));
            String color = String.valueOf(typedArray.getText(1));

            try {
                setColorFilter(Theme.getColor(color));
            } catch (NoSuchValueException e) {
                setColorFilter(Theme.getColor(getContext(), Theme.getResId(color, R.color.class)));
            }

            if (getBackground() != null) {
                try {
                    getBackground().setColorFilter(Theme.getColor(backgroundTint), PorterDuff.Mode.SRC_ATOP);
                } catch (NoSuchValueException e) {
                    getBackground().setColorFilter(Theme.getColor(getContext(), Theme.getResId(backgroundTint, R.color.class)), PorterDuff.Mode.SRC_ATOP);
                }
            }

            typedArray.recycle();
        } catch (Exception e) {
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }
    }

}
