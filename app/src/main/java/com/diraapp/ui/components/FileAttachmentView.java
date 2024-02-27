package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.utils.android.DeviceUtils;

public class FileAttachmentView extends LinearLayout {
    private final boolean isSelfMessage;
    private boolean isInit = false;

    public FileAttachmentView(@NonNull Context context, boolean isSelfMessage) {
        super(context);
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_file_attachment, this);

        int dp300 = DeviceUtils.dpToPx(240, getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp300, LayoutParams.WRAP_CONTENT);

        int dp8 = DeviceUtils.dpToPx(8, getContext());
        params.setMargins(0, dp8, 2 * dp8, dp8);

        this.setLayoutParams(params);

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
