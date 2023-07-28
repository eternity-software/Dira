package com.diraapp.api.updates;

/**
 * Ask all room members to send their BaseMember status
 */
public class PingUpdate extends Update {
    public PingUpdate() {
        super(0, UpdateType.PING_UPDATE);
    }
}
