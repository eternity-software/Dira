package com.diraapp.exceptions;

import com.diraapp.db.entities.messages.Message;

/**
 * Throws when an incorrect ViewType passed to ViewTypeFactory
 */
public class UnknownViewTypeException extends RuntimeException {

    public UnknownViewTypeException(Message message) {
        super(message.toString());
    }

    public UnknownViewTypeException(int type) {
        super(String.valueOf(type));
    }
}
