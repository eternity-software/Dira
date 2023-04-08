package com.diraapp.api.updates;


import com.diraapp.db.entities.Message;

public class NewMessageUpdate extends Update {

    private Message message;

    public NewMessageUpdate(Message message) {
        super(0, UpdateType.NEW_MESSAGE_UPDATE);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
