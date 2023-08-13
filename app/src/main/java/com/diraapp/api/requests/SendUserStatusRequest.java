package com.diraapp.api.requests;

import com.diraapp.userstatus.UserStatus;

public class SendUserStatusRequest extends Request {

    private UserStatus userStatus;

    public SendUserStatusRequest(UserStatus userStatus) {
        super(0, RequestType.USER_STATUS_REQUEST);
        this.userStatus = userStatus;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
