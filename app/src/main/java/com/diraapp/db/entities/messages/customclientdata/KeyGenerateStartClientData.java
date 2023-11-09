package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

public class KeyGenerateStartClientData extends CustomClientData {

    public KeyGenerateStartClientData() {
        this.setMessageType(MessageType.KEY_GENERATE_START);
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.key_generate_start);
    }

}
