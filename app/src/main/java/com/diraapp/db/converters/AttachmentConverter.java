package com.diraapp.db.converters;

import androidx.room.TypeConverter;

import com.diraapp.db.entities.Attachment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AttachmentConverter {
    @TypeConverter
    public static ArrayList<Attachment> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Attachment>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(ArrayList<Attachment> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
