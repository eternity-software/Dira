package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.NoMessageContainerViewHolder;

public class EmojiMessageViewHolder extends NoMessageContainerViewHolder {
    TextView emojiText;


    public EmojiMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);

        emojiText = itemView.findViewById(R.id.emoji_view);
        isInitialised = true;
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
