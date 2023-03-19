package ru.dira.api.requests;

public class Request {

    private long requestId;
    private final RequestType requestType;

    public Request(long requestId, RequestType requestType) {
        this.requestId = requestId;
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}
