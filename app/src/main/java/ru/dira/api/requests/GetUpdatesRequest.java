package ru.dira.api.requests;

public class GetUpdatesRequest extends Request {

    private String roomSecret;
    private long fromUpdateId;

    public GetUpdatesRequest(String roomSecret, long fromUpdateId) {
        super(0, RequestType.GET_UPDATES);
        this.roomSecret = roomSecret;
        this.fromUpdateId = fromUpdateId;
    }

    public long getFromUpdateId() {
        return fromUpdateId;
    }

    public void setFromUpdateId(long fromUpdateId) {
        this.fromUpdateId = fromUpdateId;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }
}
