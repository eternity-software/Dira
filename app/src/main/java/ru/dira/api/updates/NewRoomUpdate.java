package ru.dira.api.updates;

import ru.dira.api.InviteRoom;


public class NewRoomUpdate extends Update {

    private InviteRoom inviteRoom;

    public NewRoomUpdate( InviteRoom inviteRoom) {
        super(0, UpdateType.ROOM_UPDATE);
        this.inviteRoom = inviteRoom;
    }

    public InviteRoom getInviteRoom() {
        return inviteRoom;
    }

    public void setInviteRoom(InviteRoom inviteRoom) {
        this.inviteRoom = inviteRoom;
    }
}
