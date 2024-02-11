package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
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

    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) AND " +
            "attachmentType = :type AND " +
            "id > :newestId ORDER BY id LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getNewerAttachments(String roomSecret, long newestId, AttachmentType type);

    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) AND " +
            "attachmentType = :type AND " +
            "id < :oldestId ORDER BY id DESC LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getOlderAttachments(String roomSecret, long oldestId, AttachmentType type);

    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) AND " +
            "attachmentType = :type " +
            "ORDER BY id DESC LIMIT " + ATTACHMENT_LOAD_COUNT)
    List<Attachment> getLatestAttachments(String roomSecret, AttachmentType type);

}
