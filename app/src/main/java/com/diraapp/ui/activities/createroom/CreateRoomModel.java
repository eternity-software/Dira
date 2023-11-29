package com.diraapp.ui.activities.createroom;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.utils.KeyGenerator;

import java.util.ArrayList;

public class CreateRoomModel implements CreateRoomContract.Model {

    private final RoomDao roomDao;

    public CreateRoomModel(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    @Override
    public void createRoom(String roomName, String roomSecret, String welcomeMessage,
                           String authorId, String authorName, String serverAddress, int updateExpireSec) {

        Room room = new Room(roomName, System.currentTimeMillis(), roomSecret,
                serverAddress, true, new ArrayList<>(), new ArrayList<>());

        room.setUpdateExpireSec(updateExpireSec);
        roomDao.insertAll(room);

        Message message = new Message();
        message.setText(welcomeMessage);
        message.setAuthorId(authorId);
        message.setAuthorNickname(authorName);
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);

        SendMessageRequest sendMessageRequest = new SendMessageRequest(message, updateExpireSec);

        UpdateProcessor.getInstance().sendSubscribeRequest();
        try {
            UpdateProcessor.getInstance().sendRequest(sendMessageRequest, serverAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
