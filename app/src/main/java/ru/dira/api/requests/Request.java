package ru.dira.api.requests;

public class Request {

    private long requestId;
    private RequestType requestType;

    public Request(long requestId, RequestType requestType) {
        this.requestId = requestId;
        this.requestType = requestType;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public long getRequestId() {
        return requestId;
    }
}
