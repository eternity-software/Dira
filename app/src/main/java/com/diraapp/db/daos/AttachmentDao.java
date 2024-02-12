package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;

import java.util.List;

@Dao
public interface AttachmentDao {

    public static final int ATTACHMENT_LOAD_COUNT = 60;


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

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) AND " +
            "id > :newestId ORDER BY id LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getNewerAttachments(String roomSecret, long newestId, AttachmentType[] types);

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) AND " +
            "id < :oldestId ORDER BY id DESC LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getOlderAttachments(String roomSecret, long oldestId, AttachmentType[] types);

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) " +
            "ORDER BY id DESC LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getLatestAttachments(String roomSecret, AttachmentType[] types);

}
