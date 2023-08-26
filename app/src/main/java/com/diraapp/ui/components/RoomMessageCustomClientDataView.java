package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ColorTheme;
import com.masoudss.lib.WaveformSeekBar;

public class RoomMessageCustomClientDataView extends LinearLayout {


    private boolean isInit = false;

    public RoomMessageCustomClientDataView(Context context) {
        super(context);
        initView();
    }


    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_custom_clientdata, this);

        isInit = true;
    }
}
