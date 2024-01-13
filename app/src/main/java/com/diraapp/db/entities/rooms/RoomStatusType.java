package com.diraapp.db.entities.rooms;

import com.google.gson.annotations.SerializedName;

public enum RoomStatusType {

    @SerializedName("1")
    SECURE,

    @SerializedName("2")
    UNSAFE,

    @SerializedName("3")
    EMPTY
}
