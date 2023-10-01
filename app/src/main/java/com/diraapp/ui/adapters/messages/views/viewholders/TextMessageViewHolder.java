package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.AlreadyInitializedException;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;


public class TextMessageViewHolder extends BaseMessageViewHolder {

    TextView messageText;

    public TextMessageViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
    }


    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

        messageText = itemView.findViewById(R.id.message_text);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
