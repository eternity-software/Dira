package com.diraapp.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.utils.Numbers;

public class BubbleMessageView extends CardView {

    public static final int BUBBLE_CONTAINER_ID = 642376;
    private boolean isInit = false;

    public BubbleMessageView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        if (isInit) return;

        int side = Numbers.dpToPx(200, this.getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(side, side);
        this.setLayoutParams(params);
        this.setRadius(side);
        this.setCardElevation(0);
        this.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.gray));
        this.setId(BUBBLE_CONTAINER_ID);


        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_bubble, this);

        isInit = true;
    }
}
