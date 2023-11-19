package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.utils.android.DeviceUtils;

public class BubbleMessageView extends CardView {

    public static final int BUBBLE_CONTAINER_ID = 642376;
    private boolean isInit = false;

    private boolean isSelfMessage;

    public BubbleMessageView(@NonNull Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    private void initView() {
        if (isInit) return;

        int side = DeviceUtils.dpToPx(200, this.getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(side, side);
        this.setLayoutParams(params);
        this.setRadius(side);
        this.setCardElevation(0);
        this.setCardBackgroundColor(ContextCompat.getColor(this.getContext(), R.color.gray));
        this.setId(BUBBLE_CONTAINER_ID);


        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.message_bubble, this);

        int indicatorColor = Theme.getColor(getContext(), R.color.message_voice_play);
        if (isSelfMessage)
            indicatorColor = Theme.getColor(getContext(), R.color.self_message_voice_play);

        findViewById(R.id.listened_indicator).getBackground().
                setColorFilter(indicatorColor, PorterDuff.Mode.SRC_ATOP);

        isInit = true;
    }
}
