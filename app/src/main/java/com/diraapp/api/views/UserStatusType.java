package com.diraapp.api.views;

import com.google.gson.annotations.SerializedName;

public enum UserStatusType {
    @SerializedName("0")
    TYPING,
    @SerializedName("1")
    PICKING_FILE,
    @SerializedName("2")
    SENDING_FILE,
    @SerializedName("3")
    RECORDING_VOICE,
    @SerializedName("4")
    RECORDING_BUBBLE
}
