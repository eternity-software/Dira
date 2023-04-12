package com.diraapp.api.requests;

public class Request {

    private final RequestType requestType;
    private long requestId;

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
