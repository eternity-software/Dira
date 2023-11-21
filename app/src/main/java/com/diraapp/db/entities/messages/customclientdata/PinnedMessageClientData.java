package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;

import com.diraapp.R;
import com.diraapp.db.entities.messages.MessageType;

@Entity
public class PinnedMessageClientData extends CustomClientData {

    private String messageId;

    private String roomSecret;

    private String userId;

    private boolean isAddition;

    @Ignore
    public PinnedMessageClientData(String messageId, String roomSecret,
                                   String userId, boolean isAddition) {
        this.messageId = messageId;
        this.roomSecret = roomSecret;
        this.userId = userId;
        this.isAddition = isAddition;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAddition() {
        return isAddition;
    }

    public void setAddition(boolean addition) {
        isAddition = addition;
    }

    @Override
    public String getText(Context context) {
        if (isAddition) {
            return context.getString(R.string.room_update_add_pinned);

        } else {
            return context.getString(R.string.room_update_remove_pinned);
        }
    }

    public String getPinnedText(Context context, String userName) {
        if (isAddition) {
            return context.getString(R.string.room_update_add_pinned_user)
                    .replace("%s", userName);

        } else {
            return context.getString(R.string.room_update_remove_pinned_user)
                    .replace("%s", userName);
        }
    }
}
