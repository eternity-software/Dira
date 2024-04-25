package com.diraapp.ui.adapters.messages.legacy;

import com.diraapp.db.entities.messages.Message;

public interface MessageReplyListener {

    void onReplyClicked(Message message, Message holderMessage);
}
