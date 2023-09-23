package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.diraapp.utils.KeyGenerator;

import org.jetbrains.annotations.NotNull;

@Entity
public class MessageReply {

    @PrimaryKey
    @NotNull
    private String replyId;

    private String messageId;

    @Ignore
    private Message repliedMessage;

    @Ignore
    public MessageReply(String messageId) {
        this.messageId = messageId;

        replyId = KeyGenerator.generateId();
    }

    public MessageReply() {

    }

    @NotNull
    public String getReplyId() {
        return replyId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setReplyId(@NotNull String replyId) {
        this.replyId = replyId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Message getRepliedMessage() {
        return repliedMessage;
    }

    public void setRepliedMessage(Message repliedMessage) {
        this.repliedMessage = repliedMessage;
    }
}
