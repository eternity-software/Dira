package com.diraapp.db.converters;

import androidx.room.TypeConverter;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.CustomClientData;
import com.diraapp.db.entities.messages.MessageType;
import com.diraapp.db.entities.messages.RoomIconChange;
import com.diraapp.db.entities.messages.RoomNameChange;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CustomClientDataConverter {

    @TypeConverter
    public static CustomClientData fromString(String string) {
        if (string == null) return null;
        if (string.equals("null")) return null;

        Gson gson = new Gson();
        CustomClientData clientData = gson.fromJson(string, CustomClientData.class);

        if (clientData.getMessageType().equals(MessageType.ROOM_NAME_CHANGE_MESSAGE)) {
            return gson.fromJson(string, RoomNameChange.class);
        } else if (clientData.getMessageType().equals(MessageType.ROOM_ICON_CHANGE_MESSAGE)){
            return gson.fromJson(string, RoomIconChange.class);
        }
        return null;
    }

    @TypeConverter
    public static String fromCustomClientData(CustomClientData clientData) {
        Gson gson = new Gson();
        String json = null;

        if (clientData instanceof RoomNameChange) {
            json = gson.toJson((RoomNameChange) clientData);
        } else if (clientData instanceof RoomIconChange) {
            json = gson.toJson((RoomIconChange) clientData);
        }

        return json;
    }
}
