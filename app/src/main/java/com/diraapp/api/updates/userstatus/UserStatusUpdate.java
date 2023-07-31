package com.diraapp.api.updates.userstatus;

import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;

public class UserStatusUpdate extends Update {

    private static final long TIME_EXPIRE_SEC = 5;

    private Status status;

    public UserStatusUpdate(Status status) {
        super(0, UpdateType.USER_STATUS_UPDATE);
        this.setUpdateExpireSec(TIME_EXPIRE_SEC);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
