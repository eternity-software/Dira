package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.res.Theme;

public class MessageAttachmentToLargeView extends LinearLayout {


    TextView sizeText;
    TextView attachmentTooLargeText;
    TextView buttonDownload;
    private boolean isInit = false;
    private boolean isSelfMessage;

    public MessageAttachmentToLargeView(Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initLayout();
    }

    public MessageAttachmentToLargeView(Context context) {
        super(context);
    }

    private void initLayout() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_message_attachment_too_large, this);


        int titleColor = Theme.getColor(getContext(), R.color.atl_title_color);
        int textColor = Theme.getColor(getContext(), R.color.atl_text_color);
        int buttonColor = Theme.getColor(getContext(), R.color.atl_button_color);
        int buttonTextColor = Theme.getColor(getContext(), R.color.atl_button_text_color);

        if (isSelfMessage) {
            titleColor = Theme.getColor(getContext(), R.color.self_atl_title_color);
            textColor = Theme.getColor(getContext(), R.color.self_atl_text_color);
            buttonColor = Theme.getColor(getContext(), R.color.self_atl_button_color);
            buttonTextColor = Theme.getColor(getContext(), R.color.self_atl_button_text_color);
        }

        sizeText = findViewById(R.id.size_view);
        attachmentTooLargeText = findViewById(R.id.attachment_too_large_text);
        buttonDownload = findViewById(R.id.download_button);

        buttonDownload.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.SRC_ATOP);
        buttonDownload.setTextColor(buttonTextColor);

        sizeText.setTextColor(textColor);
        attachmentTooLargeText.setTextColor(titleColor);

        isInit = true;
    }

}
