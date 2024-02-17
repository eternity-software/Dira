package com.diraapp.ui.components.mediatypeselector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.diraapp.R;
import com.diraapp.utils.Logger;

import java.util.ArrayList;

public class MediaTypeSelector extends LinearLayout {

    private final ArrayList<MediaTypeSelectorItem> views = new ArrayList<>();
    private int currentSelected;

    private MediaTypeSelectorListener listener;

    public MediaTypeSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setListener(MediaTypeSelectorListener listener) {
        this.listener = listener;
    }

    public void setPosition(int position) {
        onChildClicked(position);
    }

    private void init(Context context, AttributeSet attrs) {
        checkAttrs(context, attrs);

        setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                if (!(view1 instanceof MediaTypeSelectorItem)) return;
                MediaTypeSelectorItem item = (MediaTypeSelectorItem) view1;

                initItem(item);
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                //
            }
        });
    }

    private void initItem(MediaTypeSelectorItem item) {
        final int finalI = views.size();
        views.add(item);
        item.setSelected(finalI == currentSelected);

        item.setOnClickListener((View v) -> {
            onChildClicked(finalI);
        });
    }

    @SuppressLint("ResourceType")
    private void checkAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = null;
        try {
            int[] sets = {R.attr.selected};
            typedArray = context.obtainStyledAttributes(attrs, sets);

            currentSelected = typedArray.getIndex(0);

            typedArray.recycle();
        } catch (Exception e) {
            currentSelected = 0;
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }
    }

    private void onChildClicked(int position) {
        if (currentSelected == position) return;
        Logger.logDebug("DiraSelector", "selected - " + position + " prev - " + currentSelected);

        views.get(currentSelected).setSelected(false);
        views.get(position).setSelected(true);
        currentSelected = position;

        if (listener != null) listener.onSelected(currentSelected);
    }
}
