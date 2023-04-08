package com.diraapp.db.entities;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.diraapp.db.converters.AttachmentConverter;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;

@Entity
@TypeConverters({AttachmentConverter.class})
public class Message {

    @PrimaryKey
    @NotNull
    private String id;
    private String authorId;
    private String roomSecret;
    private String text;
    private String authorNickname;
    private long time;
    private ArrayList<Attachment> attachments = new ArrayList<>();

    @Ignore
    public Message(String authorId, String text, String authorNickname) {
        this.authorId = authorId;
        this.id = KeyGenerator.generateId();
        this.text = text;
        this.authorNickname = authorNickname;
    }

    public Message() {

    }

    public static Message generateMessage(Context context, String roomSecret) {
        Message message = new Message();

        message.setAuthorId(CacheUtils.getInstance().getString(CacheUtils.ID, context));
        message.setAuthorNickname(CacheUtils.getInstance().getString(CacheUtils.NICKNAME, context));
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);
        return message;

    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }
}
