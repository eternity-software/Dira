package com.diraapp.exceptions;

import com.diraapp.ui.adapters.messages.views.viewholders.factories.MessageHolderType;

/**
 * Throws when a ViewHolder already has been initialized
 */
public class AlreadyInitializedException extends RuntimeException {

    public AlreadyInitializedException(MessageHolderType messageHolderType) {
        super("ViewHolder " + messageHolderType + " already initialized");
    }
}
