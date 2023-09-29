package com.diraapp.ui.adapters.messages.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.BaseViewHolder;
import com.diraapp.ui.components.MessageReplyComponent;

public class TextMessageViewHolder extends BaseViewHolder {

    TextView messageText;
    TextView emojiText;


    public TextMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        messageText = itemView.findViewById(R.id.message_text);
        emojiText = itemView.findViewById(R.id.emoji_view);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                getItemViewType(), isSelfMessage());
        viewsContainer.addView(replyComponent);

        setInitialised(true);
        updateViews();
        updateReplies();
    }

    @Override
    public void onBind(Message message, Message previousMessage) {

    }
}
