package ru.dira.api.requests;

public class JoinRoomRequest extends Request {

    private String invitationCode;

    public JoinRoomRequest(String invitationCode) {
        super(0, RequestType.ACCEPT_INVITE);
        this.invitationCode = invitationCode;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
}
