package com.diraapp.ui.components.dynamic;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.widget.AppCompatButton;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.Theme;
import com.diraapp.res.lang.CustomLanguage;

import org.jetbrains.annotations.NotNull;


public class DynamicButton extends AppCompatButton {
    @StyleableRes
    int localizableKey = 0;
    private CharSequence localId;

    public DynamicButton(Context context) {
        super(context);
    }

    public DynamicButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DynamicButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        int[] sets = {R.attr.localizableButtonKey, R.attr.themeButtonBackground, R.attr.themeButtonText};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);
        CharSequence locId = typedArray.getText(0);
        String themeButtonBackground = String.valueOf(typedArray.getText(1));
        String textColor = String.valueOf(typedArray.getText(2));
        localId = locId;
        Drawable backgroundShape = getBackground();
        try {
            backgroundShape.setColorFilter(Theme.getColor(themeButtonBackground), PorterDuff.Mode.SRC);
        } catch (NoSuchValueException e) {
            if(getBackground() != null) getBackground().setColorFilter(Theme.getColor(getContext(), Theme.getResId(String.valueOf(themeButtonBackground), R.color.class)), PorterDuff.Mode.SRC_ATOP);
        }

        if(typedArray.getText(2) != null) {
            try {
                setTextColor(Theme.getColor(String.valueOf(textColor)));
            } catch (NoSuchValueException e) {
                setTextColor(Theme.getColor(getContext(), Theme.getResId(String.valueOf(textColor), R.color.class)));
            }
        }
        try {
            setText(CustomLanguage.getStringsRepository().getValue(String.valueOf(locId)));
        } catch (NoSuchValueException ignored) {
        }
        typedArray.recycle();
    }
}
