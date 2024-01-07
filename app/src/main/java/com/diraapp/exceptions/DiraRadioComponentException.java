package com.diraapp.exceptions;

import com.diraapp.ui.components.DiraRadioComponent;
import com.diraapp.ui.components.DiraRadioComponentItem;

public class DiraRadioComponentException extends RuntimeException {

    public DiraRadioComponentException() {
        super(DiraRadioComponent.class.getName() +
                ": 0 " + DiraRadioComponentItem.class.getName() + " has been found");
    }
}
