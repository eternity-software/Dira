package com.diraapp.db.converters;

import androidx.room.TypeConverter;

import com.diraapp.db.entities.messages.MessageReading;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MessageReadingConverter {

    @TypeConverter
    public static ArrayList<MessageReading> fromString(String value) {
        Type listType = new TypeToken<ArrayList<MessageReading>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<MessageReading> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

}
