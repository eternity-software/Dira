package com.diraapp.api.requests;

import com.diraapp.api.views.UserStatus;
import com.diraapp.userstatus.Status;

public class SendUserStatusRequest extends Request {

    private Status status;

    public SendUserStatusRequest(Status status) {
        super(0, RequestType.USER_STATUS_REQUEST);
        this.status = status;
    }

    public Status getUserStatus() {
        return status;
    }

    public void setUserStatus(Status status) {
        this.status = status;
    }
}
