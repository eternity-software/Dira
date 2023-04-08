package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import com.diraapp.db.entities.Message;

@Dao
public interface MessageDao {

    @Insert
    void insertAll(Message... messages);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM message WHERE roomSecret = :roomSecret ORDER BY time DESC")
    List<Message> getAllMessageByUpdatedTime(String roomSecret);

    @Query("SELECT * FROM message WHERE id = :messageId")
    Message getMessageById(String messageId);

}