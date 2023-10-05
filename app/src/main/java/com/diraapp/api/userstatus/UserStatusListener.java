package com.diraapp.api.userstatus;

import java.util.ArrayList;

public interface UserStatusListener {

    void updateUserStatus(String secretName, ArrayList<UserStatus> usersUserStatusList);
}
