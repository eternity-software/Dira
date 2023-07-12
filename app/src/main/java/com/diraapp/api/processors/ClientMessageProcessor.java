package com.diraapp.api.processors;

import android.content.Context;

import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.entities.messages.CustomClientData;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.RoomJoinClientData;
import com.diraapp.db.entities.messages.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.RoomNameChangeClientData;
import com.diraapp.notifications.Notifier;
import com.diraapp.utils.DiraApplication;

public class ClientMessageProcessor {

    private final Context context;

    public ClientMessageProcessor(Context context) {
        this.context = context;
    }

    public Message notifyMemberAdded(MemberUpdate memberUpdate) {
        RoomJoinClientData joining = new RoomJoinClientData(memberUpdate.getNickname());
        return notifyClientMessage(memberUpdate, joining);
    }

    public Message notifyRoomNameChange(RoomUpdate roomUpdate, String oldName) {
        RoomNameChangeClientData roomNameChange =
                new RoomNameChangeClientData(roomUpdate.getName(), oldName);
        return notifyClientMessage(roomUpdate, roomNameChange);
    }

    public Message notifyRoomIconChange(RoomUpdate update, String path) {
        RoomIconChangeClientData roomIconChangeClientData = new RoomIconChangeClientData(path);
        return notifyClientMessage(update, roomIconChangeClientData);
    }

    public Message notifyRoomMessageAndIconChanged(RoomUpdate update, String oldNickname, String path) {
        RoomNameAndIconChangeClientData roomNameAndIconChangeClientData =
                new RoomNameAndIconChangeClientData(update.getName(), oldNickname, path);
        return notifyClientMessage(update, roomNameAndIconChangeClientData);
    }

    private Message notifyClientMessage(Update update, CustomClientData clientData) {
        Message message = new Message(update.getRoomSecret(), clientData);
        NewMessageUpdate messageUpdate = new NewMessageUpdate(message);

        if (DiraApplication.isBackgrounded()) {
            Notifier.notifyMessage(((NewMessageUpdate) update).getMessage(), context);
        }
        UpdateProcessor.getInstance().notifyUpdateListeners(messageUpdate);

        return message;
    }
}
