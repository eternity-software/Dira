package com.diraapp.db.converters;

import androidx.room.TypeConverter;

import com.diraapp.db.entities.messages.MessageReply;
import com.google.gson.Gson;

public class MessageReplyConverter {

    @TypeConverter
    public static MessageReply fromJson(String value) {
        return new Gson().fromJson(value, MessageReply.class);
    }

    @TypeConverter
    public static String toJson(MessageReply reply) {
        Gson gson = new Gson();
        return gson.toJson(reply);
    }
}
