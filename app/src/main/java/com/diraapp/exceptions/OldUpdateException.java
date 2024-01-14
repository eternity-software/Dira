package com.diraapp.exceptions;

/**
 * Exception occurs when newer update was applied to room
 */
public class OldUpdateException extends Exception {

    public OldUpdateException(String roomName, String roomId, long gottenId, long lastRoomId) {
        super(roomName + " (" + roomId + ") - gottenId = " + gottenId + ", lastRoomUpdateId = " + lastRoomId);

    }

}
