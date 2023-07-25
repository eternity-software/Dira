package com.diraapp.api.updates;


import com.diraapp.api.views.DhKey;

public class KeyReceivedUpdate extends Update {
    private DhKey dhKey;

    public KeyReceivedUpdate(DhKey dhKey) {
        super(0, UpdateType.KEY_RECEIVED_UPDATE);
        this.dhKey = dhKey;
    }

    public DhKey getDhKey() {
        return dhKey;
    }

    public void setDhKey(DhKey dhKey) {
        this.dhKey = dhKey;
    }
}
