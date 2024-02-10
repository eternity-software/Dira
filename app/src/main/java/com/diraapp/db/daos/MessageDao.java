package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.diraapp.db.daos.auxiliaryobjects.MessageWithAttachments;
import com.diraapp.db.entities.messages.Message;

import java.util.List;

@Dao
public interface MessageDao {

    int LOADING_COUNT = 50;

    int LOADING_COUNT_HALF = LOADING_COUNT / 2;

    @Insert
    void insertAll(Message... messages);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    // Returns only Message Object
    @Query("SELECT * FROM message WHERE id = :messageId")
    Message getMessageById(String messageId);


    // with attachments
    default Message getMessageAndAttachmentsById(String messageId) {
        MessageWithAttachments messageWithAttachments = getMessageWithAttachmentsById(messageId);
        if (messageWithAttachments == null) return null;
        return messageWithAttachments.getMessage();
    }

    @Query("SELECT * FROM message WHERE id = :messageId")
    MessageWithAttachments getMessageWithAttachmentsById(String messageId);


    @Deprecated
    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC")
    List<Message> getAllMessages(String roomSecret);


    default List<Message> getBeforeMessagesInRoom(String roomSecret, Long beforeTime) {
        return MessageWithAttachments.convertList(getBeforeMessagesWithAttachmentsInRoom(roomSecret, beforeTime));
    }

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time < :beforeTime ORDER BY time DESC LIMIT " + LOADING_COUNT)
    List<MessageWithAttachments> getBeforeMessagesWithAttachmentsInRoom(String roomSecret, Long beforeTime);


    default List<Message> getNewerMessages(String roomSecret, long time) {
        return MessageWithAttachments.convertList(getNewerMessagesWithAttachments(roomSecret, time));
    }

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time > :time ORDER BY time LIMIT " + LOADING_COUNT)
    List<MessageWithAttachments> getNewerMessagesWithAttachments(String roomSecret, long time);


    default List<Message> getLatestMessagesInRoom(String roomSecret) {
        return MessageWithAttachments.convertList(getLatestMessagesWithAttachmentsInRoomD(roomSecret));
    }

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC LIMIT " + LOADING_COUNT)
    List<MessageWithAttachments> getLatestMessagesWithAttachmentsInRoomD(String roomSecret);


    default List<Message> getBeforePartOnRoomLoading(String roomSecret, Long beforeTime) {
        return MessageWithAttachments.convertList(getBeforePartOnRoomLoadingWithAttachments(roomSecret, beforeTime));
    }

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time < :beforeTime ORDER BY time DESC LIMIT " + LOADING_COUNT_HALF)
    List<MessageWithAttachments> getBeforePartOnRoomLoadingWithAttachments(String roomSecret, Long beforeTime);


    default List<Message> getNewerPartOnRoomLoading(String roomSecret, long time) {
        return MessageWithAttachments.convertList(getNewerPartOnRoomLoadingWithAttachments(roomSecret, time));
    }

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time >= :time ORDER BY time LIMIT " + LOADING_COUNT_HALF)
    List<MessageWithAttachments> getNewerPartOnRoomLoadingWithAttachments(String roomSecret, long time);


}