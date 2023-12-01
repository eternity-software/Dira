package com.diraapp.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.diraapp.R;
import com.diraapp.utils.Logger;

import java.util.ArrayList;

public class DiraRadioComponent extends LinearLayout {

    private int currentSelected;

    private final ArrayList<DiraRadioComponentItem> views = new ArrayList<>();

    public DiraRadioComponent(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        checkAttrs(context, attrs);

        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View view, View view1) {
                if (!(view1 instanceof DiraRadioComponentItem)) return;
                DiraRadioComponentItem item = (DiraRadioComponentItem) view1;

                initItem(item);
            }

            @Override
            public void onChildViewRemoved(View view, View view1) {
                //
            }
        });
    }

    private void initItem(DiraRadioComponentItem item) {
        final int finalI = views.size();
        views.add(item);
        if (finalI == currentSelected) item.setSelected(true);

        item.setOnClickListener((View v) -> {
            if (currentSelected == finalI) return;
            Logger.logDebug("dfdfdfd", "selected - " + finalI + " prev - " + currentSelected);

            views.get(currentSelected).setSelected(false);
//            this.setSelected(true); I don't know why, but it doesn't work
            views.get(finalI).setSelected(true);
            currentSelected = finalI;
        });
    }

    @SuppressLint("ResourceType")
    private void checkAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = null;
        try {
            int[] sets = {R.attr.default_selected};
            typedArray = context.obtainStyledAttributes(attrs, sets);

            currentSelected = typedArray.getIndex(0);

            typedArray.recycle();
        } catch (Exception e) {
            currentSelected = 0;
            if (typedArray != null) typedArray.recycle();
            e.printStackTrace();
        }
    }

    public int getCurrentSelected() {
        return currentSelected;
    }
}
