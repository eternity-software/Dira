package com.diraapp.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;

public class RoomMediaMessage extends LinearLayout {

    private boolean isInit = false;

    public RoomMediaMessage(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.message_media, this);

        isInit = true;
    }
}
