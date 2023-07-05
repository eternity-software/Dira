package com.diraapp.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.diraapp.db.converters.AttachmentConverter;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;


@Database(entities = {Message.class, Room.class, Member.class, Attachment.class},
        autoMigrations = {@AutoMigration(from = 6, to = 7)},
        version = 7,
        exportSchema = true)
@TypeConverters({AttachmentConverter.class})
public abstract class DiraMessageDatabase extends RoomDatabase {

    public static final String DB_NAME = "messages_db";

    public static DiraMessageDatabase getDatabase(Context applicationContext) {
        return androidx.room.Room.databaseBuilder(applicationContext,
                DiraMessageDatabase.class, DB_NAME).build();
    }

    public abstract MessageDao getMessageDao();

    public abstract AttachmentDao getAttachmentDao();
}