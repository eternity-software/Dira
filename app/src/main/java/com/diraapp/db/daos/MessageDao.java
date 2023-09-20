package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.diraapp.db.entities.messages.Message;

import java.util.List;

@Dao
public interface MessageDao {

    static final int LOADING_COUNT = 50;

    static final int LOADING_COUNT_HALF = LOADING_COUNT / 2;

    @Insert
    void insertAll(Message... messages);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC")
    List<Message> getAllMessages(String roomSecret);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time < :beforeTime ORDER BY time DESC LIMIT " + LOADING_COUNT)
    List<Message> getBeforeMessagesInRoom(String roomSecret, Long beforeTime);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time > :time ORDER BY time LIMIT " + LOADING_COUNT)
    List<Message> getNewerMessages(String roomSecret, long time);

    @Query("SELECT * FROM message WHERE id = :messageId")
    Message getMessageById(String messageId);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC LIMIT " + LOADING_COUNT)
    List<Message> getLatestMessagesInRoom(String roomSecret);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time < :beforeTime ORDER BY time DESC LIMIT " + LOADING_COUNT_HALF)
    List<Message> getBeforePartOnRoomLoading(String roomSecret, Long beforeTime);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time >= :time ORDER BY time LIMIT " + LOADING_COUNT_HALF)
    List<Message> getNewerPartOnRoomLoading(String roomSecret, long time);


}