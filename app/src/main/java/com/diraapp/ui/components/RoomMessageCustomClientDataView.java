package com.diraapp.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.diraapp.R;

public class RoomMessageCustomClientDataView extends LinearLayout {


    private boolean isInit = false;

    public RoomMessageCustomClientDataView(Context context) {
        super(context);
        initView();
    }


    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_custom_clientdata, this);

        isInit = true;
    }
}
