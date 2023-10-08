package com.diraapp.api.userstatus;

import com.diraapp.api.views.UserStatusType;

public class UserStatus {

    public static final long VISIBLE_TIME_MILLIS = 1000;

    public static final long REQUEST_DELAY = 700;

    private final UserStatusType userStatusType;

    private final String userId;

    private final String secretName;

    private long time;

    public UserStatus(UserStatusType userStatusType, String userId, String secretName) {
        this.userStatusType = userStatusType;
        this.userId = userId;
        this.secretName = secretName;
    }

    public String getSecretName() {
        return secretName;
    }

    public UserStatusType getUserStatus() {
        return userStatusType;
    }

    public String getUserId() {
        return userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
