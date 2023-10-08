package com.diraapp.ui.adapters.messages.views.viewholders;

import com.diraapp.db.entities.messages.Message;

public class DelayedMessageBind {
    private Message message;
    private Message previousMessage;

    public DelayedMessageBind(Message message, Message previousMessage) {
        this.message = message;
        this.previousMessage = previousMessage;
    }

    public Message getPreviousMessage() {
        return previousMessage;
    }

    public void setPreviousMessage(Message previousMessage) {
        this.previousMessage = previousMessage;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
