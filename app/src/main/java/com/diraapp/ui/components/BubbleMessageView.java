package com.diraapp.ui.components;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.diraapp.R;
import com.diraapp.ui.appearance.ColorTheme;

public class BubbleMessageView extends CardView {

    private boolean isInit = false;

    public BubbleMessageView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_bubble, this);

        isInit = true;
    }
}
