package com.diraapp.db.entities.messages;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.diraapp.db.converters.AttachmentConverter;
import com.diraapp.db.converters.CustomClientDataConverter;
import com.diraapp.db.converters.MessageReadingConverter;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.customclientdata.CustomClientData;
import com.diraapp.exceptions.UnknownAuthorException;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Entity
@TypeConverters({AttachmentConverter.class, CustomClientDataConverter.class,
        MessageReadingConverter.class})
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
    @ColumnInfo(defaultValue = "null")
    private String repliedMessageId;

    @Ignore
    private Message repliedMessage;

    @ColumnInfo(defaultValue = "true")
    private boolean isRead = true;

    @ColumnInfo(defaultValue = "0")
    private long lastTimeEncryptionKeyUpdated;

    private ArrayList<MessageReading> messageReadingList = new ArrayList<>();

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

        this.isRead = true;
    }

    public Message() {

    }

    public static Message generateMessage(CacheUtils cacheUtils, String roomSecret) {
        Message message = new Message();

        message.setAuthorId(cacheUtils.getString(CacheUtils.ID));
        message.setAuthorNickname(cacheUtils.getString(CacheUtils.NICKNAME));
        message.setId(KeyGenerator.generateId());
        message.setRoomSecret(roomSecret);

        message.isRead = true;
        return message;

    }

    public boolean isReadable() {
        return !isRead() && !hasCustomClientData() && hasAuthor();
    }

    public boolean isSameDay(Message messageToCompare) {
        Date date = new Date(getTime());
        Date dateCompare = new Date(messageToCompare.getTime());

        Calendar calendar = Calendar.getInstance();
        Calendar calendarPrev = Calendar.getInstance();

        calendar.setTime(date);
        calendarPrev.setTime(dateCompare);

        return calendar.get(Calendar.DAY_OF_YEAR) == calendarPrev.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isSameYear(Message messageToCompare) {
        Date date = new Date(getTime());
        Date dateCompare = new Date(messageToCompare.getTime());

        Calendar calendar = Calendar.getInstance();
        Calendar calendarPrev = Calendar.getInstance();

        calendar.setTime(date);
        calendarPrev.setTime(dateCompare);

        return calendar.get(Calendar.YEAR) == calendarPrev.get(Calendar.YEAR);
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

    public boolean hasAuthor() {
        return authorId != null;
    }

    public String getAuthorId() {
        if (authorId == null) throw new UnknownAuthorException();
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
        if (text == null) {
            text = "";
        }
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

    public boolean hasCustomClientData() {
        return customClientData != null;
    }

    public CustomClientData getCustomClientData() {
        return customClientData;
    }

    public void setCustomClientData(CustomClientData customClientData) {
        this.customClientData = customClientData;
    }

    public ArrayList<MessageReading> getMessageReadingList() {
        return messageReadingList;
    }

    public void setMessageReadingList(ArrayList<MessageReading> messageReadingList) {
        this.messageReadingList = messageReadingList;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getRepliedMessageId() {
        return repliedMessageId;
    }

    public void setRepliedMessageId(String repliedMessageId) {
        this.repliedMessageId = repliedMessageId;
    }

    public Message getRepliedMessage() {
        return repliedMessage;
    }

    public void setRepliedMessage(Message repliedMessage) {
        this.repliedMessage = repliedMessage;
    }
}
