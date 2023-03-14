package ru.dira.api.updates;


import java.util.List;

public class ServerSyncUpdate extends Update {

    private List<String> supportedApis;
    private long timeServerStart;

    public ServerSyncUpdate() {
        super(0, UpdateType.SERVER_SYNC);
    }

    public void setSupportedApis(List<String> supportedApis) {
        this.supportedApis = supportedApis;
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
}
