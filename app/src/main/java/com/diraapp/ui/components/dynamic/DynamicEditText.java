package com.diraapp.ui.components.dynamic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.widget.AppCompatEditText;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.lang.CustomLanguage;

import org.jetbrains.annotations.NotNull;


public class DynamicEditText extends AppCompatEditText {

    @StyleableRes
    int localizableKey = 0;
    private CharSequence localId;

    public DynamicEditText(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DynamicEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @SuppressLint("ResourceType")
    private void initialize(Context context, AttributeSet attrs) {
        int[] sets = {R.attr.localizableHint};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);
        CharSequence locId = typedArray.getText(localizableKey);
        localId = locId;
        try {
            setHint(CustomLanguage.getStringsRepository().getValue(String.valueOf(locId)));
        } catch (NoSuchValueException ignored) {
        }
        typedArray.recycle();
    }
}
