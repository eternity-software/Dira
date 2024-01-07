package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;

public class FileAttachmentComponent extends FrameLayout {
    private boolean isInit = false;
    private final boolean isSelfMessage;

    public FileAttachmentComponent(@NonNull Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_file_attachment, this);

        if (isSelfMessage) {
            findViewById(R.id.message_file_attachment_icon_background).
                    getBackground().setColorFilter(
                            Theme.getColor(getContext(), R.color.self_message_file_background),
                            PorterDuff.Mode.SRC_IN);

            ((ThemeImageView) findViewById(R.id.message_file_attachment_icon)).setColorFilter(
                    Theme.getColor(getContext(), R.color.self_message_file),
                    PorterDuff.Mode.SRC_IN);

            ((DynamicTextView) findViewById(R.id.message_file_attachment_name)).
                    setTextColor(Theme.getColor(getContext(), R.color.self_message_color));

            ((DynamicTextView) findViewById(R.id.message_file_attachment_size)).
                    setTextColor(Theme.getColor(getContext(), R.color.self_message_file_size));
        }

        isInit = true;
    }
}
