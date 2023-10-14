package com.diraapp.api.userstatus;

import java.util.ArrayList;

public interface UserStatusListener {

    void updateRoomStatus(String secretName, ArrayList<UserStatus> usersUserStatusList);
}
