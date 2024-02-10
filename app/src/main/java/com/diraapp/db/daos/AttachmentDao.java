package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.diraapp.db.entities.Attachment;

import java.util.List;

@Dao
public interface AttachmentDao {


    @Insert
    void insertAll(Attachment... attachments);

    @Update
    int update(Attachment attachment);

    @Delete
    void delete(Attachment attachment);

    @Query("SELECT * FROM attachment WHERE id = :id")
    Attachment getAttachmentById(long id);

    @Query("SELECT * FROM attachment WHERE message_id = :messageId")
    List<Attachment> getAttachmentsByMessageId(String messageId);

}
