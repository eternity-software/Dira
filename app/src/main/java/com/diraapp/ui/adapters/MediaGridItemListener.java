package com.diraapp.ui.adapters;

import android.view.View;

import com.diraapp.ui.bottomsheet.filepicker.SelectorFileInfo;

import java.util.List;

public interface MediaGridItemListener {
    void onItemClick(int pos, View view);

    void onLastItemLoaded(int pos, View view);

    default void onItemSelected(SelectorFileInfo selectorFileInfo, List<SelectorFileInfo> selectorFileInfoList) {
    }
}