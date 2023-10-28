package com.diraapp.ui.adapters.messages.views.viewholders.factories;

public enum MessageHolderType {


    SELF_TEXT_MESSAGE(true),
    ROOM_TEXT_MESSAGE(false),
    SELF_BUBBLE_MESSAGE(true),
    ROOM_BUBBLE_MESSAGE(false),
    SELF_VOICE_MESSAGE(true),
    ROOM_VOICE_MESSAGE(false),
    SELF_SINGLE_ATTACHMENT_MESSAGE(true),
    ROOM_SINGLE_ATTACHMENT_MESSAGE(false),
    SELF_GROUP_ATTACHMENTS_MESSAGE(true),
    ROOM_GROUP_ATTACHMENTS_MESSAGE(false),
    SELF_EMOJI_MESSAGE(true),
    ROOM_EMOJI_MESSAGE(false),
    SELF_FILE_ATTACHMENT(true),
    ROOM_FILE_ATTACHMENT(false),
    ROOM_UPDATES(false);
    private final boolean isSelf;

    MessageHolderType(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public boolean isSelf() {
        return isSelf;
    }
}
