package com.diraapp.api.updates;


import java.util.List;

public class ServerSyncUpdate extends Update {

    private List<String> supportedApis;
    private long timeServerStart;

    private String fileServerUrl;

    public ServerSyncUpdate() {
        super(0, UpdateType.SERVER_SYNC);
    }

    public String getFileServerUrl() {
        return fileServerUrl;
    }

    public void setFileServerUrl(String fileServerUrl) {
        this.fileServerUrl = fileServerUrl;
    }

    public long getTimeServerStart() {
        return timeServerStart;
    }

    public void setTimeServerStart(long timeServerStart) {
        this.timeServerStart = timeServerStart;
    }

    public List<String> getSupportedApis() {
        return supportedApis;
    }

    public void setSupportedApis(List<String> supportedApis) {
        this.supportedApis = supportedApis;
    }
}
