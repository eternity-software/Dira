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
    NEW_ROOM_UPDATE,
    @SerializedName("8")
    PING_UPDATE,
    @SerializedName("9")
    BASE_MEMBER_UPDATE,
    @SerializedName("10")
    DIFFIE_HELLMAN_INIT_UPDATE,
    @SerializedName("11")
    KEY_RECEIVED_UPDATE,
    @SerializedName("12")
    RENEWING_CONFIRMED,
    @SerializedName("13")
    RENEWING_CANCEL,
    @SerializedName("14")
    READ_UPDATE,
    @SerializedName("15")
    USER_STATUS_UPDATE,
    @SerializedName("16")
    ATTACHMENT_LISTENED_UPDATE,
    @SerializedName("17")
    PINNED_MESSAGE_ADDED_UPDATE,
    @SerializedName("18")
    PINNED_MESSAGE_REMOVED_UPDATE
}

