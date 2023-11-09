package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

public class UnencryptedMessageClientData extends CustomClientData {

    public UnencryptedMessageClientData() {
        this.setMessageType(MessageType.UNENCRYPTED_USER_MESSAGE);
    }

    @Override
    public String getText(Context context) {
        return context.getString(R.string.unencrypted_user_message);
    }
}
