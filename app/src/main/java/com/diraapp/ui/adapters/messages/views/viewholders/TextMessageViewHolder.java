package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.WithMessageContainerViewHolder;

public class TextMessageViewHolder extends WithMessageContainerViewHolder {

    TextView messageText;


    public TextMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

        messageText = itemView.findViewById(R.id.message_text);
        isInitialised = true;
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
