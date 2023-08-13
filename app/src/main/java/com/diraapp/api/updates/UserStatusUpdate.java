package com.diraapp.api.updates;

import com.diraapp.userstatus.UserStatus;

public class UserStatusUpdate extends Update {

    private static final long TIME_EXPIRE_SEC = 5;

    private UserStatus userStatus;

    public UserStatusUpdate(UserStatus userStatus) {
        super(0, UpdateType.USER_STATUS_UPDATE);
        this.setUpdateExpireSec(TIME_EXPIRE_SEC);
        this.userStatus = userStatus;
    }

    public UserStatus getStatus() {
        return userStatus;
    }

    public void setStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
