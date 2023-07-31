package com.diraapp.api.processors;

import android.content.Context;

import com.diraapp.api.updates.DhInitUpdate;
import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.RenewingConfirmUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.customclientdata.CustomClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.notifications.Notifier;
import com.diraapp.utils.DiraApplication;

public class ClientMessageProcessor {

    private final Context context;

    public ClientMessageProcessor(Context context) {
        this.context = context;
    }

    public Message notifyMemberAdded(MemberUpdate memberUpdate, String path) {
        RoomJoinClientData joining = new RoomJoinClientData(memberUpdate.getNickname(), path);
        Room room = DiraRoomDatabase.getDatabase(context).getRoomDao().
                getRoomBySecretName(memberUpdate.getRoomSecret());
        return notifyClientMessage(memberUpdate, joining, room);
    }

    public Message notifyRoomNameChange(RoomUpdate roomUpdate, String oldName, Room room) {
        RoomNameChangeClientData roomNameChange =
                new RoomNameChangeClientData(roomUpdate.getName(), oldName);
        return notifyClientMessage(roomUpdate, roomNameChange, room);
    }

    public Message notifyRoomIconChange(RoomUpdate update, String path, Room room) {
        RoomIconChangeClientData roomIconChangeClientData = new RoomIconChangeClientData(path);
        return notifyClientMessage(update, roomIconChangeClientData, room);
    }

    public Message notifyRoomMessageAndIconChange(RoomUpdate update, String oldNickname, String path, Room room) {
        RoomNameAndIconChangeClientData roomNameAndIconChangeClientData =
                new RoomNameAndIconChangeClientData(update.getName(), oldNickname, path);
        return notifyClientMessage(update, roomNameAndIconChangeClientData, room);
    }

    public Message notifyRoomKeyGenerationStart(DhInitUpdate update, Room room) {
        KeyGenerateStartClientData clientData =
                new KeyGenerateStartClientData();
        return notifyClientMessage(update, clientData, room);
    }

    public Message notifyRoomKeyGenerationStop(Update update, Room room) {
        KeyGeneratedClientData clientData;

        Short code;

        if (update instanceof RenewingConfirmUpdate) {
            code = KeyGeneratedClientData.RESULT_SUCCESS;
        } else {
            code = KeyGeneratedClientData.RESULT_CANCELLED;
        }

        clientData = new KeyGeneratedClientData(code);
        return notifyClientMessage(update, clientData, room);
    }

    private Message notifyClientMessage(Update update, CustomClientData clientData, Room room) {

        Message message = new Message(update.getRoomSecret(), clientData);
        NewMessageUpdate messageUpdate = new NewMessageUpdate(message);

        if (DiraApplication.isBackgrounded()) {
            Notifier.notifyMessage(((NewMessageUpdate) update).getMessage(), room, context);
        }
        UpdateProcessor.getInstance().notifyUpdateListeners(messageUpdate);

        return message;
    }
}
