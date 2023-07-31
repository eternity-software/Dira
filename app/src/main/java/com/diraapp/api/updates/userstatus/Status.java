package com.diraapp.api.updates.userstatus;

import com.diraapp.api.views.UserStatus;

public class Status {

    public static final long VISIBLE_TIME_MILLIS = 500;

    private final UserStatus userStatus;

    private final String userId;

    private long time;

    public Status(UserStatus userStatus, String userId) {
        this.userStatus = userStatus;
        this.userId = userId;
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
