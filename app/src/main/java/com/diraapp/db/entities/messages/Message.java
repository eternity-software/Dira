package com.diraapp.db.entities.messages;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.diraapp.db.converters.AttachmentConverter;
import com.diraapp.db.converters.CustomClientDataConverter;
import com.diraapp.db.entities.Attachment;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity
@TypeConverters({AttachmentConverter.class, CustomClientDataConverter.class})
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
    private CustomClientData customClientData = null;
    @ColumnInfo(defaultValue = "0")
    private long lastTimeEncryptionKeyUpdated;

    @Ignore
    public Message(String authorId, String text, String authorNickname) {
        this.authorId = authorId;
        this.id = KeyGenerator.generateId();
        this.text = text;
        this.authorNickname = authorNickname;
    }

    @Ignore
    public Message(String roomSecret, CustomClientData clientData) {
        this.roomSecret = roomSecret;
        this.customClientData = clientData;
        this.time = System.currentTimeMillis();
        this.id = KeyGenerator.generateId();
    }

    public Message() {

    }

    public static Message generateMessage(Context context, String roomSecret) {
        Message message = new Message();

        CacheUtils cacheUtils = new CacheUtils(context);

        message.setAuthorId(cacheUtils.getString(CacheUtils.ID));
        message.setAuthorNickname(cacheUtils.getString(CacheUtils.NICKNAME));
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);
        return message;

    }

    public long getLastTimeEncryptionKeyUpdated() {
        return lastTimeEncryptionKeyUpdated;
    }

    public void setLastTimeEncryptionKeyUpdated(long lastTimeEncryptionKeyUpdated) {
        this.lastTimeEncryptionKeyUpdated = lastTimeEncryptionKeyUpdated;
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

    public CustomClientData getCustomClientData() {
        return customClientData;
    }

    public void setCustomClientData(CustomClientData customClientData) {
        this.customClientData = customClientData;
    }
}
