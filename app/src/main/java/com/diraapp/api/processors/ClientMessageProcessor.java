package com.diraapp.api.processors;

import android.content.Context;

import com.diraapp.DiraApplication;
import com.diraapp.api.updates.DhInitUpdate;
import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.PinnedMessageAddedUpdate;
import com.diraapp.api.updates.PinnedMessageRemovedUpdate;
import com.diraapp.api.updates.RenewingConfirmUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.customclientdata.CustomClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.PinnedMessageClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.notifications.Notifier;

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

    public Message notifyRoomNameChange(RoomUpdate roomUpdate, String oldName, Room room, boolean needNotify) {
        RoomNameChangeClientData roomNameChange =
                new RoomNameChangeClientData(roomUpdate.getName(), oldName);
        return notifyClientMessage(roomUpdate, roomNameChange, room, needNotify);
    }

    public Message notifyRoomIconChange(RoomUpdate update, String path, Room room, boolean needNotify) {
        RoomIconChangeClientData roomIconChangeClientData = new RoomIconChangeClientData(path);
        return notifyClientMessage(update, roomIconChangeClientData, room, needNotify);
    }

    public Message notifyRoomMessageAndIconChange(RoomUpdate update, String oldNickname,
                                                  String path, Room room, boolean needNotify) {
        RoomNameAndIconChangeClientData roomNameAndIconChangeClientData =
                new RoomNameAndIconChangeClientData(update.getName(), oldNickname, path);
        return notifyClientMessage(update, roomNameAndIconChangeClientData, room, needNotify);
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

    public Message notifyPinnedMessageAdded(Update update, Room room) {
        PinnedMessageClientData clientData;
        if (update instanceof PinnedMessageAddedUpdate) {
            PinnedMessageAddedUpdate p = (PinnedMessageAddedUpdate) update;
            clientData = new PinnedMessageClientData(p.getMessageId(), p.getRoomSecret(), p.getUserId(), true);
        } else {
            PinnedMessageRemovedUpdate p = (PinnedMessageRemovedUpdate) update;
            clientData = new PinnedMessageClientData(p.getMessageId(), p.getRoomSecret(), p.getUserId(), false);
        }

        return notifyClientMessage(update, clientData, room);
    }

    private Message notifyClientMessage(Update update, CustomClientData clientData, Room room) {
        return notifyClientMessage(update, clientData, room, true);
    }

    private Message notifyClientMessage(Update update, CustomClientData clientData, Room room, boolean needNotify) {

        Message message = new Message(update.getRoomSecret(), clientData);
        NewMessageUpdate messageUpdate = new NewMessageUpdate(message);

        if (needNotify) {
            if (DiraApplication.isBackgrounded()) {
                Notifier.notifyMessage(message, room, context);
            }
            UpdateProcessor.getInstance().notifyUpdateListeners(messageUpdate);
        }

        return message;
    }
}
