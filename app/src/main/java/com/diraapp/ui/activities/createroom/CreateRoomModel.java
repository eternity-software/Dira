package com.diraapp.ui.activities.createroom;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.api.updates.SubscribeUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomStatusType;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.utils.KeyGenerator;

import java.util.ArrayList;

public class CreateRoomModel implements CreateRoomContract.Model {

    private final RoomDao roomDao;

    public CreateRoomModel(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public void createRoom(String roomName, String roomSecret, String welcomeMessage,
                           String authorId, String authorName, String serverAddress,
                           RoomType roomType, int updateExpireSec) {

        Room room = new Room(roomName, System.currentTimeMillis(), roomSecret,
                serverAddress, true, new ArrayList<>(), new ArrayList<>(), roomType);

        if (roomType == RoomType.PRIVATE) room.setRoomStatusType(RoomStatusType.EMPTY);
        room.setUpdateExpireSec(updateExpireSec);
        roomDao.insertAll(room);

        Message message = new Message();
        message.setText(welcomeMessage);
        message.setAuthorId(authorId);
        message.setAuthorNickname(authorName);
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);

        SendMessageRequest sendMessageRequest = new SendMessageRequest(message, updateExpireSec);

        try {
            UpdateProcessor.getInstance().addUpdateListener(new UpdateListener() {
                @Override
                public void onUpdate(Update update) {
                    if (update.getUpdateType() != UpdateType.SUBSCRIBE_UPDATE) return;

                    SubscribeUpdate syncUpdate = (SubscribeUpdate) update;

                    if (syncUpdate.getSubscribedRoomSecrets().contains(roomSecret)) {
                        try {
                            UpdateProcessor.getInstance().sendRequest(sendMessageRequest, serverAddress);
                        } catch (Exception ignored) {
                        }
                        UpdateProcessor.getInstance().removeUpdateListener(this);
                    }
                }
            });

            UpdateProcessor.getInstance().sendSubscribeRequest();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
