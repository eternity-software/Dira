package com.diraapp.api.requests;

import com.google.gson.annotations.SerializedName;

public enum RequestType {
    @SerializedName("0")
    KEEPALIVE,
    @SerializedName("1")
    SUBSCRIBE_REQUEST,
    @SerializedName("2")
    VERIFY_ROOM_INFO,
    @SerializedName("3")
    SEND_MESSAGE_REQUEST,
    @SerializedName("4")
    GET_UPDATES,
    @SerializedName("5")
    UPDATE_ROOM,
    @SerializedName("6")
    UPDATE_MEMBER,
    @SerializedName("7")
    CREATE_INVITE,
    @SerializedName("8")
    ACCEPT_INVITE,
    @SerializedName("9")
    PING_MEMBERS,
    @SerializedName("10")
    PING_REACT,
    @SerializedName("11")
    KEY_RENEW_REQUEST,
    @SerializedName("12")
    SEND_INTERMEDIATE_KEY,
    @SerializedName("13")
    SUBMIT_KEY,

    @SerializedName("14")
    MESSAGE_READ,

    @SerializedName("15")
    USER_STATUS_REQUEST
}
