package com.diraapp.db.daos.auxiliaryobjects;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageWithAttachments {

    @Embedded
    private Message message;

    @Relation(
            parentColumn = "id",
            entityColumn = "message_id"
    )
    private List<Attachment> attachments;

    public Message getMessage() {
        message.setAttachments(new ArrayList<>(attachments));
        return message;
    }

    public static List<Message> convertList(List<MessageWithAttachments> withAttachmentsList) {
        ArrayList<Message> messages = new ArrayList<>(withAttachmentsList.size());

        for (MessageWithAttachments withAttachments: withAttachmentsList) {
            messages.add(withAttachments.getMessage());
        }
        return messages;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
