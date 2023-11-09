package com.diraapp.db.entities.messages;

import com.google.gson.annotations.SerializedName;

public enum MessageType {

    @SerializedName("0")
    USER_MESSAGE,

    @SerializedName("1")
    ROOM_ICON_CHANGE_MESSAGE,

    @SerializedName("2")
    ROOM_NAME_CHANGE_MESSAGE,

    @SerializedName("3")
    ROOM_NAME_AND_ICON_CHANGE_MESSAGE,

    @SerializedName("4")
    NEW_USER_ROOM_JOINING,

    @SerializedName("5")
    KEY_GENERATE_START,

    @SerializedName("6")
    KEY_GENERATED,

    @SerializedName("7")
    UNENCRYPTED_USER_MESSAGE
}
