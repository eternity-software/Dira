package com.diraapp.ui.components.dynamic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.res.Theme;


public class DynamicCardView extends CardView {

    private String backgroundTint;

    public DynamicCardView(Context context) {
        super(context);
    }

    public DynamicCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DynamicCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        updateTint();
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        updateTint();
    }

    private void updateTint() {
        if (getBackground() != null) {

            try {
                getBackground().setColorFilter(Theme.getColor(String.valueOf(backgroundTint)), PorterDuff.Mode.SRC_ATOP);
                // getBackground().setT(Theme.getColor(String.valueOf(color)));
            } catch (NoSuchValueException e) {
                getBackground().setColorFilter(Theme.getColor(getContext(), Theme.getResId(String.valueOf(backgroundTint), R.color.class)), PorterDuff.Mode.SRC_ATOP);
            }
            requestLayout();
        }
    }

    private void initialize(Context context, AttributeSet attrs) {
        int[] sets = {R.attr.themeBackgroundCardTint};
        TypedArray typedArray = context.obtainStyledAttributes(attrs, sets);


        backgroundTint = typedArray.getString(0);


        updateTint();
        typedArray.recycle();
    }
}
