package com.diraapp.ui.components;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;

public class RoomMessageVideoPlayer extends CardView {

    private boolean isInit = false;

    public RoomMessageVideoPlayer(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_videoplayer, this);

        isInit = true;
    }
}
