package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diraapp.R;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.ui.appearance.ColorTheme;

import java.io.File;

public class MessageAttachmentToLargeView extends LinearLayout {

    private ColorTheme theme;

    private boolean isInit = false;

    private boolean isSelfMessage;

    TextView sizeText;

    TextView attachmentTooLargeText;

    TextView buttonDownload;

    public MessageAttachmentToLargeView(Context context, ColorTheme theme, boolean isSelfMessage) {
        super(context);
        this.theme = theme;
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

        sizeText = findViewById(R.id.size_view);
        attachmentTooLargeText = findViewById(R.id.attachment_too_large_text);
        buttonDownload = findViewById(R.id.download_button);

        if (isSelfMessage) {
            sizeText.setTextColor(theme.getSelfLinkColor());
            attachmentTooLargeText.setTextColor(theme.getSelfTextColor());
            buttonDownload.getBackground().setColorFilter(theme.getSelfDownButtonColor(), PorterDuff.Mode.SRC_IN);
            buttonDownload.setTextColor(theme.getSelfDownloadButtonTextColor());
        } else {
            sizeText.setTextColor(theme.getRoomLickColor());
            attachmentTooLargeText.setTextColor(theme.getTextColor());
            buttonDownload.getBackground().setColorFilter(theme.getDownloadButtonColor(), PorterDuff.Mode.SRC_IN);
            buttonDownload.setTextColor(theme.getDownloadButtonTextColor());
        }



        isInit = true;
    }

}
