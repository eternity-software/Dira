package com.diraapp.api.updates;

public class NewInvitationUpdate extends Update {

    private String invitationCode;

    public NewInvitationUpdate(String invitationCode) {
        super(0, UpdateType.ROOM_CREATE_INVITATION);
        this.invitationCode = invitationCode;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
}
