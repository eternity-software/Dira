package com.diraapp.db.entities.messages.customclientdata;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.diraapp.db.entities.messages.MessageType;
import com.diraapp.utils.StringFormatter;

import org.jetbrains.annotations.NotNull;

@Entity
public class CustomClientData {

    @PrimaryKey(autoGenerate = true)
    @NotNull
    private long id;

    private MessageType messageType;

    public CustomClientData() {

    }

    @NotNull
    public long getId() {
        return id;
    }

    public void setId(@NotNull long id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getText(Context context) {
        return StringFormatter.EMPTY_STRING;
    }
}
