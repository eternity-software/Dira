package com.diraapp.userstatus;

import android.content.Context;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.updates.UserStatusUpdate;
import com.diraapp.exceptions.SingletonException;

import java.util.ArrayList;
import java.util.HashMap;

public class UserStatusHandler implements UpdateListener {

    private static final long TIME_TO_SLEEP = 10;

    private static UserStatusHandler instance;

    private boolean keepThread = true;

    private ArrayList<Status> userStatuses = new ArrayList<>();

    private Thread statusThread;

    private ArrayList<UserStatusListener> listenerList = new ArrayList<>();

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
        ArrayList<Status> list = new ArrayList<>();
        for (Status status: userStatuses) {
            if (status.getSecretName().equals(secretName)) list.add(status);
        }
        return list;
    }

    public void startThread() {
        if (statusThread != null) {
            if (statusThread.isAlive()) return;
        }
        startUserStatusThread();
    }

    public void killThread() {
        keepThread = false;
    }

    public void addListener(UserStatusListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(UserStatusListener listener) {
        listenerList.remove(listener);
    }

    private void notifyListeners(String secretName) {
        for (UserStatusListener listener: listenerList) {
            listener.updateUserStatus(secretName, getUserStatuses(secretName));
        }
    }

    private void startUserStatusThread() {

        statusThread = new Thread(() -> {
            ArrayList<Status> listToDelete = new ArrayList<>();
            long minTime = -1;
            while (keepThread) {

                ArrayList<Status> userStatusList = new ArrayList<>(userStatuses);
                int size = userStatusList.size();
                if (size == 0) {
                    try {
                        Thread.sleep(TIME_TO_SLEEP);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                for (Status status: userStatusList) {
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

                for (Status status: listToDelete) {
                    userStatusList.remove(status);
                    notifyListeners(status.getSecretName());
                }
            }
        });
        statusThread.start();
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.USER_STATUS_UPDATE) {
            Status status = ((UserStatusUpdate) update).getStatus();
            userStatuses.add(status);
            notifyListeners(status.getSecretName());
        }
    }
}
