package com.diraapp.api.updates;

import com.google.gson.annotations.SerializedName;

public enum UpdateType {
    @SerializedName("0")
    ACCEPTED_STATUS,
    @SerializedName("1")
    SERVER_SYNC,
    @SerializedName("2")
    SUBSCRIBE_UPDATE,
    @SerializedName("3")
    NEW_MESSAGE_UPDATE,
    @SerializedName("4")
    ROOM_UPDATE,
    @SerializedName("5")
    MEMBER_UPDATE,
    @SerializedName("6")
    ROOM_CREATE_INVITATION,
    @SerializedName("7")
    NEW_ROOM_UPDATE
}

