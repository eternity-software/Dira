package ru.dira.api.updates;

import java.util.ArrayList;
import java.util.List;

public class SubscribeUpdate extends Update {

    private List<String> subscribedRoomSecrets = new ArrayList<>();

    public SubscribeUpdate(long updateId) {
        super(updateId, UpdateType.SUBSCRIBE_UPDATE);
    }

    public List<String> getSubscribedRoomSecrets() {
        return subscribedRoomSecrets;
    }

    public Update setSubscribedRoomSecrets(List<String> subscribedRoomSecrets) {
        this.subscribedRoomSecrets = subscribedRoomSecrets;
        return this;
    }
}
