package com.diraapp.ui.activities.createroom;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.utils.KeyGenerator;

public class CreateRoomModel implements CreateRoomContract.Model {

    private final RoomDao roomDao;

    public CreateRoomModel(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public void createRoom(String roomName, String roomSecret, String welcomeMessage,
                           String authorId, String authorName, String serverAddress) {

        Room room = new Room(roomName, System.currentTimeMillis(), roomSecret, serverAddress, true);

        roomDao.insertAll(room);

        Message message = new Message();
        message.setText(welcomeMessage);
        message.setAuthorId(authorId);
        message.setAuthorNickname(authorName);
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);

        SendMessageRequest sendMessageRequest = new SendMessageRequest(message);

        UpdateProcessor.getInstance().sendSubscribeRequest();
        try {
            UpdateProcessor.getInstance().sendRequest(sendMessageRequest, serverAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
