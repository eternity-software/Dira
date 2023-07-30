package com.diraapp.db.entities.messages.customclientdata;

import com.diraapp.db.entities.messages.MessageType;

public class KeyGenerateStartClientData extends CustomClientData {

    public KeyGenerateStartClientData() {
        this.setMessageType(MessageType.KEY_GENERATE_START);
    }
}
