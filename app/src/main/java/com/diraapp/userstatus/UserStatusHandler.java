package com.diraapp.userstatus;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.updates.UserStatusUpdate;
import com.diraapp.exceptions.SingletonException;

import java.util.ArrayList;

public class UserStatusHandler implements UpdateListener {
    private static UserStatusHandler instance;

    private final ArrayList<UserStatus> userUserStatuses = new ArrayList<>();
    private final ArrayList<UserStatusListener> listenerList = new ArrayList<>();
    private Thread statusThread;

    public UserStatusHandler() throws SingletonException {
        if (instance == null) {
            instance = this;
            UpdateProcessor.getInstance().addUpdateListener(this);
            return;
        }

        throw new SingletonException();
    }

    public static UserStatusHandler getInstance() {
        if (instance == null) {
            try {
                return new UserStatusHandler();
            } catch (SingletonException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public ArrayList<UserStatus> getUserStatuses(String secretName) {
        ArrayList<String> usersIds = new ArrayList<>();
        ArrayList<UserStatus> list = new ArrayList<>();

        int size = userUserStatuses.size();
        if (size > 0) {
            int stop = 3;

            for (int i = size - 1; i >= 0; i--) {
                UserStatus userStatus = userUserStatuses.get(i);
                if (userStatus.getSecretName().equals(secretName)) {
                    if (!usersIds.contains(userStatus.getUserId())) {
                        list.add(userStatus);
                        usersIds.add(userStatus.getUserId());
                        if (list.size() == stop) break;
                    }
                }
            }
        }
        return list;
    }

    public void startThread() {
        if (statusThread == null) {
            initUserStatusThread();
            statusThread.start();
        } else if (!statusThread.isAlive()) {
            initUserStatusThread();
            statusThread.start();
        }
    }

    public void addListener(UserStatusListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(UserStatusListener listener) {
        listenerList.remove(listener);
    }

    private void notifyListeners(String secretName) {
        for (UserStatusListener listener : listenerList) {
            listener.updateUserStatus(secretName, getUserStatuses(secretName));
        }
    }

    private void initUserStatusThread() {
        statusThread = new Thread(() -> {
            ArrayList<UserStatus> listToDelete = new ArrayList<>();
            System.out.println("User status thread started");
            long minTime = -1;
            while (true) {
                ArrayList<UserStatus> userUserStatusList = new ArrayList<>(userUserStatuses);
                int size = userUserStatusList.size();
                if (size == 0) {
                    break;
                }

                for (UserStatus userStatus : userUserStatusList) {
                    minTime = userUserStatusList.get(0).getTime();
                    listToDelete.add(userUserStatusList.get(0));

                    if (userStatus.getTime() < minTime) {
                        listToDelete.clear();
                        minTime = userStatus.getTime();
                        listToDelete.add(userStatus);
                    } else if (userStatus.getTime() == minTime) {
                        listToDelete.add(userStatus);
                    }
                }

                long current = System.currentTimeMillis();
                if (minTime > current) {
                    try {
                        Thread.sleep(minTime - current);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (UserStatus userStatus : listToDelete) {
                    userUserStatuses.remove(userStatus);
                    notifyListeners(userStatus.getSecretName());
                }
            }
        });
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.USER_STATUS_UPDATE) {
            UserStatus userStatus = ((UserStatusUpdate) update).getStatus();
            userStatus.setTime(System.currentTimeMillis() + UserStatus.VISIBLE_TIME_MILLIS);
            userUserStatuses.add(userStatus);
            notifyListeners(userStatus.getSecretName());
            startThread();
        }
    }
}
