package com.diraapp.exceptions;

import com.diraapp.ui.components.DiraRadioComponentItem;
import com.diraapp.ui.components.DiraRadioComponent;

public class DiraRadioComponentException extends RuntimeException {

    public DiraRadioComponentException() {
        super(DiraRadioComponent.class.getName() +
                ": 0 " + DiraRadioComponentItem.class.getName() + " has been found");
    }
}
