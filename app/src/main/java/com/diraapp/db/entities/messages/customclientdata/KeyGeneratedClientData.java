package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

import java.util.Objects;

public class KeyGeneratedClientData extends CustomClientData {

    public static Short RESULT_SUCCESS = 0;
    public static Short RESULT_CANCELLED = 1;
    public static Short RESULT_USER_DISCONNECT_ERROR = 2;

    private final Short result;

    public KeyGeneratedClientData(Short result) {
        this.setMessageType(MessageType.KEY_GENERATED);
        this.result = result;
    }

    public Short getResult() {
        return result;
    }

    @Override
    public String getText(Context context) {
        String text = "";

        if (Objects.equals(result, RESULT_SUCCESS)) {
            text = context.getString(R.string.key_generate_success);
        } else if (Objects.equals(result, RESULT_CANCELLED)) {
            text = context.getString(R.string.key_generate_time_error);
        } else if (Objects.equals(result, RESULT_USER_DISCONNECT_ERROR)) {
            text = context.getString(R.string.key_generate_user_disconnected);
        }

        return text;
    }
}
