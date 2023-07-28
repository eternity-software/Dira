package com.diraapp.db.converters;

import androidx.room.TypeConverter;

import com.diraapp.db.entities.messages.MessageType;
import com.diraapp.db.entities.messages.customclientdata.CustomClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.google.gson.Gson;

public class CustomClientDataConverter {

    @TypeConverter
    public static CustomClientData fromString(String string) {
        if (string == null) return null;
        if (string.equals("null")) return null;

        Gson gson = new Gson();
        CustomClientData clientData = gson.fromJson(string, CustomClientData.class);

        if (clientData.getMessageType().equals(MessageType.ROOM_NAME_CHANGE_MESSAGE)) {
            return gson.fromJson(string, RoomNameChangeClientData.class);
        } else if (clientData.getMessageType().equals(MessageType.ROOM_ICON_CHANGE_MESSAGE)) {
            return gson.fromJson(string, RoomIconChangeClientData.class);
        } else if (clientData.getMessageType().equals(MessageType.NEW_USER_ROOM_JOINING)) {
            return gson.fromJson(string, RoomJoinClientData.class);
        } else if (clientData.getMessageType().equals(MessageType.ROOM_NAME_AND_ICON_CHANGE_MESSAGE)) {
            return gson.fromJson(string, RoomNameAndIconChangeClientData.class);
        }
        return null;
    }

    @TypeConverter
    public static String fromCustomClientData(CustomClientData clientData) {
        Gson gson = new Gson();
        String json = null;

        if (clientData instanceof RoomNameChangeClientData) {
            json = gson.toJson(clientData);
        } else if (clientData instanceof RoomIconChangeClientData) {
            json = gson.toJson(clientData);
        } else if (clientData instanceof RoomJoinClientData) {
            json = gson.toJson(clientData);
        } else if (clientData instanceof RoomNameAndIconChangeClientData) {
            json = gson.toJson(clientData);
        }

        return json;
    }
}
