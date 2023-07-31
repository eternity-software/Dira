package com.diraapp.api.requests;

import com.diraapp.api.views.UserStatus;

public class SendUserStatusRequest extends Request {

    private UserStatus userStatus;

    private String userId;

    public SendUserStatusRequest(String userId, UserStatus userStatus) {
        super(0, RequestType.USER_STATUS_REQUEST);
        this.userId = userId;
        this.userStatus = userStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
