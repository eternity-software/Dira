package com.diraapp.userstatus;

import com.diraapp.api.views.UserStatus;

public class Status {

    public static final long VISIBLE_TIME_MILLIS = 500;

    public static final long REQUEST_DELAY = 300;

    private final UserStatus userStatus;

    private final String userId;

    private final String secretName;

    private long time;

    public Status(UserStatus userStatus, String userId, String secretName) {
        this.userStatus = userStatus;
        this.userId = userId;
        this.secretName = secretName;
    }

    public String getSecretName() {
        return secretName;
    }

    public UserStatus getUserStatus() {
        return userStatus;
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
