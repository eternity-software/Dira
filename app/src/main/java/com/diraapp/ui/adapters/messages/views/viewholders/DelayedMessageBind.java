package com.diraapp.ui.adapters.messages.views.viewholders;

import com.diraapp.db.entities.messages.Message;

/**
 * Holds binding data until view not inflated
 * Used in MessageAdapter's BaseViewHolder
 */
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
