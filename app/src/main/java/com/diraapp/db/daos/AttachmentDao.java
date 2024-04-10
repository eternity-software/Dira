package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;

import java.util.List;

@Dao
public interface AttachmentDao {

    int ATTACHMENT_LOAD_COUNT = 60;


    @Insert
    void insertAll(Attachment... attachments);

    @Update
    int update(Attachment attachment);

    @Delete
    void delete(Attachment attachment);

    @Query("SELECT * FROM attachment WHERE id = :id")
    Attachment getAttachmentById(long id);

    @Query("SELECT * FROM attachment WHERE fileUrl = :fileURL")
    Attachment getAttachmentByUrl(String fileURL);

    @Query("SELECT * FROM attachment WHERE id = :id")
    AttachmentMessagePair getAttachmentMessagePairById(long id);

    @Query("SELECT * FROM attachment WHERE message_id = :messageId AND attachmentType != 'LINK'")
    List<Attachment> getAttachmentsByMessageIdWithOutLinks(String messageId);

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) AND " +
            "id > :newestId ORDER BY id LIMIT :count")
    List<AttachmentMessagePair> getNewerAttachments(String roomSecret, long newestId, AttachmentType[] types, int count);

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) AND " +
            "id < :oldestId ORDER BY id DESC LIMIT :count")
    List<AttachmentMessagePair> getOlderAttachments(String roomSecret, long oldestId, AttachmentType[] types, int count);

    @Transaction
    @Query("SELECT * FROM attachment WHERE " +
            "(SELECT roomSecret FROM message WHERE id = message_id) = :roomSecret AND " +
            "attachmentType IN (:types) " +
            "ORDER BY id DESC LIMIT :count")
    List<AttachmentMessagePair> getLatestAttachments(String roomSecret, AttachmentType[] types, int count);

}
