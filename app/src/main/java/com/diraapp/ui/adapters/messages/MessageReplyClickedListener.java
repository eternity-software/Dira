package com.diraapp.ui.adapters.messages;

import com.diraapp.db.entities.messages.Message;

public interface MessageReplyClickedListener {

    void onClicked(Message message);
}
