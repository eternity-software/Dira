package com.diraapp.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.res.Theme;
import com.diraapp.ui.adapters.messages.RoomMessagesAdapter;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.utils.Numbers;

public class MessageReplyComponent extends FrameLayout {

    private final int messageType;

    private final boolean isSelfMessage;

    private boolean isInit = false;

    public MessageReplyComponent(Context context, int messageType, boolean isSelfMessage) {
        super(context);
        this.messageType = messageType;
        this.isSelfMessage = isSelfMessage;
        initView();
    }

    private void initView() {
        if (isInit) return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.message_reply_component, this);

        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout layout = this.findViewById(R.id.message_reply_container);
        if (messageType == RoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_ATTACHMENTS |
                messageType == RoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_MULTI_ATTACHMENTS) {
            int margin = Numbers.dpToPx(8, this.getContext());
            params.setMargins(margin / 2, (int) (1.5 * margin), 0, margin);
            layout.setLayoutParams(params);
        } else if (messageType == RoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE) {
            int padding = Numbers.dpToPx(6,this.getContext());
            layout = this.findViewById(R.id.message_reply_container);
            layout.setPadding(padding, padding, padding, padding);

            int margin = Numbers.dpToPx(4, this.getContext());
            params.setMargins(margin, margin, margin, margin);

            if (isSelfMessage) params.gravity = Gravity.RIGHT;
            layout.setLayoutParams(params);

            layout.setBackground(this.getContext().getResources().
                    getDrawable(R.drawable.rounded_accent_rectangle));
            layout.getBackground().setColorFilter(Theme.getColor(
                    this.getContext(), R.color.gray_trans), PorterDuff.Mode.SRC_ATOP);
        }

        if (isSelfMessage) {
            ((ThemeImageView) this.findViewById(R.id.message_reply_line)).setColorFilter(
                    Theme.getColor(this.getContext(), R.color.self_reply_color));
            ((TextView) this.findViewById(R.id.message_reply_author_name)).setTextColor(
                    Theme.getColor(this.getContext(), R.color.self_reply_color));
        }

        isInit = true;
    }
}
