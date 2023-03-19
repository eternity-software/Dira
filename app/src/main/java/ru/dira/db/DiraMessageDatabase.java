package ru.dira.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ru.dira.db.converters.AttachmentConverter;
import ru.dira.db.daos.AttachmentDao;
import ru.dira.db.daos.MessageDao;
import ru.dira.db.entities.Attachment;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;


@Database(entities = {Message.class, Room.class, Member.class, Attachment.class}, version = 6,
        autoMigrations = {
                @AutoMigration(from = 4, to = 5),
                @AutoMigration(from = 5, to = 6)
        },
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