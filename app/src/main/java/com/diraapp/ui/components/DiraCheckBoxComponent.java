package com.diraapp.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;

public class DiraCheckBoxComponent extends FrameLayout {

    private boolean isChecked = true;

    private ImageView check;

    private LinearLayout layout;

    public DiraCheckBoxComponent(@NonNull Context context) {
        super(context);
    }

    public DiraCheckBoxComponent(@NonNull Context context, AttributeSet attrs) {
        super(context);

        initComponent();

        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.DiraCheckBoxComponent);

            int count = typedArray.getIndexCount();

            for (int i = 0; i < count; i++) {
                int attr = typedArray.getIndex(i);

                if (attr != R.styleable.DiraCheckBoxComponent_isChecked) continue;

                isChecked = typedArray.getBoolean(attr, true);
            }

            typedArray.recycle();
        } catch (Exception e) {
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }

        initCheck();

    }

    public boolean isChecked() {
        return isChecked;
    }

    private void initComponent() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.checkbox_layout, this);

        layout = findViewById(R.id.checkbox_component_layout);
        check = findViewById(R.id.checkbox_component_check);
    }

    private void initCheck() {
        if (isChecked) return;

        check.setVisibility(GONE);

        layout.setOnClickListener((View v) -> {
            if (isChecked) {
                isChecked = false;
                hideCheck();
                return;
            }

            isChecked = true;
            showCheck();
        });
    }

    private void hideCheck() {
        // Create anim
        check.setVisibility(GONE);
    }

    private void showCheck() {
        // Create anim
        check.setVisibility(VISIBLE);
    }
}
