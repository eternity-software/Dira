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

    @Insert
    void insertAll(Message... messages);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC")
    List<Message> getAllMessages(String roomSecret);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC LIMIT 50")
    List<Message> getLastMessagesInRoom(String roomSecret);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret AND time < :beforeTime ORDER BY time DESC LIMIT 50")
    List<Message> getLastMessagesInRoom(String roomSecret, Long beforeTime);

    @Query("SELECT * FROM message WHERE id = :messageId")
    Message getMessageById(String messageId);

}