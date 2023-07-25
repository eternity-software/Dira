package com.diraapp.api.updates;

import com.google.gson.Gson;

public class UpdateDeserializer {

    public static Update deserialize(String message) {
        Update rawUpdate = new Gson().fromJson(message, Update.class);

        UpdateType updateType = rawUpdate.getUpdateType();

        switch (updateType) {
            case SERVER_SYNC:
                return new Gson().fromJson(message, ServerSyncUpdate.class);
            case SUBSCRIBE_UPDATE:
                return new Gson().fromJson(message, SubscribeUpdate.class);
            case ACCEPTED_STATUS:
                return new Gson().fromJson(message, AcceptedStatusAnswer.class);
            case NEW_MESSAGE_UPDATE:
                return new Gson().fromJson(message, NewMessageUpdate.class);
            case ROOM_UPDATE:
                return new Gson().fromJson(message, RoomUpdate.class);
            case MEMBER_UPDATE:
                return new Gson().fromJson(message, MemberUpdate.class);
            case ROOM_CREATE_INVITATION:
                return new Gson().fromJson(message, NewInvitationUpdate.class);
            case NEW_ROOM_UPDATE:
                return new Gson().fromJson(message, NewRoomUpdate.class);
            case PING_UPDATE:
                return new Gson().fromJson(message, PingUpdate.class);
            case BASE_MEMBER_UPDATE:
                return new Gson().fromJson(message, BaseMemberUpdate.class);
            case DIFFIE_HELLMAN_INIT_UPDATE:
                return new Gson().fromJson(message, DhInitUpdate.class);
            case KEY_RECEIVED_UPDATE:
                return new Gson().fromJson(message, KeyReceivedUpdate.class);
            case RENEWING_CONFIRMED:
                return new Gson().fromJson(message, RenewingConfirmUpdate.class);
            case RENEWING_CANCEL:
                return new Gson().fromJson(message, RenewingCancelUpdate.class);
        }
        return rawUpdate;
    }
}
