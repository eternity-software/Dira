package com.diraapp.ui.adapters;

import android.view.View;

public interface MediaGridItemListener {
    void onItemClick(int pos, View view);

    void onLastItemLoaded(int pos, View view);
}