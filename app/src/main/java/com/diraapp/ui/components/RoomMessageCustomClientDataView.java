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

    private ColorTheme colorTheme;

    private boolean isInit = false;

    public RoomMessageCustomClientDataView(Context context, ColorTheme colorTheme) {
        super(context);
        this.colorTheme = colorTheme;
        initView();
    }

    public RoomMessageCustomClientDataView(@NonNull Context context) {
        super(context);
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_custom_clientdata, this);


        ((TextView) findViewById(R.id.room_updates_main_text)).setTextColor(colorTheme.getTextColor());
        ((TextView) findViewById(R.id.room_updates_text)).setTextColor(colorTheme.getRoomUpdateMessageColor());

        isInit = true;
    }
}
