package com.diraapp.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;

public class DiraRadioComponentItem extends LinearLayout {

    private boolean isSelected = false;

    private TextView textView;

    private ImageView check;

    private ImageView hide;

    public DiraRadioComponentItem(Context context) {
        super(context);
    }

    public DiraRadioComponentItem(Context context, AttributeSet attrs) {
        super(context);
        init(context, attrs);
    }

    private void inflate() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.dira_radio_item, this);
    }

    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs) {
        inflate();

        textView = findViewById(R.id.dira_radio_item_text);
        check = findViewById(R.id.dira_radio_item_check);
        hide = findViewById(R.id.dira_radio_item_hide);

        setOrientation(HORIZONTAL);

        setBackground(AppCompatResources.getDrawable(getContext(),
                R.drawable.rounded_dark_background));
        getBackground().setColorFilter(Theme.getColor(getContext(),
                R.color.gray), PorterDuff.Mode.SRC_ATOP);

        int padding = DeviceUtils.dpToPx(6, getContext());

        setPadding(padding, padding, padding, padding);

        TypedArray typedArray = null;
        try {
            int[] sets = {R.attr.item_text};
            typedArray = context.obtainStyledAttributes(attrs, sets);

            String text = String.valueOf(typedArray.getText(0));

            try {
                textView.setText(text);
            } catch (Exception e) {
                textView.setText(this.getResources().getString(R.string.unknown));
                e.printStackTrace();
            }

            typedArray.recycle();
        } catch (Exception e) {
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }

    }

    public void setSelected(boolean isSelected) {
        if (this.isSelected == isSelected) return;
        this.isSelected = isSelected;

        if (isSelected) {
            textView.setTextColor(Theme.getColor(getContext(), R.color.dira_radio_text_selected));
            check.setVisibility(VISIBLE);
            hide.setVisibility(GONE);
            return;
        }

        check.setVisibility(GONE);
        hide.setVisibility(VISIBLE);
        textView.setTextColor(Theme.getColor(getContext(), R.color.dira_radio_text));
    }

}
