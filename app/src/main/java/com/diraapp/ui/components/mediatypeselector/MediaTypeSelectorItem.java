package com.diraapp.ui.components.mediatypeselector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.utils.android.DeviceUtils;

public class MediaTypeSelectorItem extends ThemeLinearLayout {

    private boolean isSelected = false;

    private DynamicTextView textView;

    private ThemeImageView imageView;

    public MediaTypeSelectorItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void inflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.media_type_selector, this);
    }

    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs) {
        inflate();

        textView = this.findViewById(R.id.text_view);
        imageView = this.findViewById(R.id.image_view);

        this.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_background));

        int dp4 = DeviceUtils.dpToPx(4, context);
        int dp10 = (int) (dp4 * 2.5);
        this.setPadding(dp10, dp4, dp10, dp4);
        this.setGravity(Gravity.CENTER_VERTICAL);

        TypedArray typedArray = null;
        try {
            int[] sets = {R.attr.itemIcon, R.attr.itemText};
            typedArray = context.obtainStyledAttributes(attrs, sets);

            int drawableId = typedArray.getResourceId(0, R.drawable.ic_image);
            imageView.setImageDrawable(ContextCompat.getDrawable(context, drawableId));

            int textId = typedArray.getResourceId(1, R.string.unknown);
            textView.setText(getResources().getString(textId));

            typedArray.recycle();
        } catch (Exception e) {
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int dp4 = DeviceUtils.dpToPx(4, getContext());
        params.setMargins(dp4, 0, dp4, 0);
        setLayoutParams(params);

        if (isSelected) {
            getBackground().setColorFilter(Theme.getColor(getContext(), R.color.accent_trans), PorterDuff.Mode.SRC_ATOP);
            textView.setTextColor(Theme.getColor(getContext(), R.color.accent));
            imageView.setColorFilter(Theme.getColor(getContext(), R.color.accent));
            return;
        }

        getBackground().setColorFilter(Theme.getColor(getContext(), R.color.dark_lighter), PorterDuff.Mode.SRC_ATOP);
        textView.setTextColor(Theme.getColor(getContext(), R.color.medium_light_light_gray));
        imageView.setColorFilter(Theme.getColor(getContext(), R.color.medium_light_light_gray));
    }
}
