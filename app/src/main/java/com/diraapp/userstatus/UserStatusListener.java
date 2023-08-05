package com.diraapp.userstatus;

import java.util.ArrayList;

public interface UserStatusListener {

    public void updateUserStatus(String secretName, ArrayList<Status> usersStatusList);
}
