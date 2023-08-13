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

    private final ArrayList<Status> userStatuses = new ArrayList<>();

    private Thread statusThread;

    private final ArrayList<UserStatusListener> listenerList = new ArrayList<>();

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

    public ArrayList<Status> getUserStatuses(String secretName) {
        ArrayList<String> usersIds = new ArrayList<>();
        ArrayList<Status> list = new ArrayList<>();

        int size = userStatuses.size();
        if (size > 0) {
            int stop = 3;

            for (int i = size - 1; i >= 0; i--) {
                Status status = userStatuses.get(i);
                if (status.getSecretName().equals(secretName)) {
                    if (!usersIds.contains(status.getUserId())) {
                        list.add(status);
                        usersIds.add(status.getUserId());
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
            ArrayList<Status> listToDelete = new ArrayList<>();
            System.out.println("User status thread started");
            long minTime = -1;
            while (true) {
                ArrayList<Status> userStatusList = new ArrayList<>(userStatuses);
                int size = userStatusList.size();
                if (size == 0) {
                    break;
                }

                for (Status status : userStatusList) {
                    minTime = userStatusList.get(0).getTime();
                    listToDelete.add(userStatusList.get(0));

                    if (status.getTime() < minTime) {
                        listToDelete.clear();
                        minTime = status.getTime();
                        listToDelete.add(status);
                    } else if (status.getTime() == minTime) {
                        listToDelete.add(status);
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

                for (Status status : listToDelete) {
                    userStatuses.remove(status);
                    notifyListeners(status.getSecretName());
                }
            }
        });
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.USER_STATUS_UPDATE) {
            Status status = ((UserStatusUpdate) update).getStatus();
            status.setTime(System.currentTimeMillis() + Status.VISIBLE_TIME_MILLIS);
            userStatuses.add(status);
            notifyListeners(status.getSecretName());
            startThread();
        }
    }
}
