package ru.dira.api.requests;

import ru.dira.db.entities.Message;

public class SendMessageRequest extends Request {

    private Message message;


    public SendMessageRequest(Message message) {
        super(0, RequestType.SEND_MESSAGE_REQUEST);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
