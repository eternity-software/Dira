package com.diraapp.api.views;

import com.google.gson.annotations.SerializedName;

public enum UserStatus {

    @SerializedName("0")
    TYPING,

    @SerializedName("1")
    CHOOSING_IMAGE,

    @SerializedName("2")
    SENDING_IMAGE
}
