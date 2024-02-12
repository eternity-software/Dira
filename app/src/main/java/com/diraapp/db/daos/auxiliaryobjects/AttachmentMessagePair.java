package com.diraapp.db.daos.auxiliaryobjects;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;

public class AttachmentMessagePair {

    @Embedded
    private Attachment attachment;

    @Relation(
            parentColumn = "message_id",
            entityColumn = "id"
    )
    private Message message;

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
