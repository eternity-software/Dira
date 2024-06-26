package com.diraapp.db;

import static com.diraapp.db.migrations.MessageMigrationFrom28to29.MIGRATION_FROM_28_TO_29;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.diraapp.db.converters.CustomClientDataConverter;
import com.diraapp.db.converters.MessageReadingConverter;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.db.entities.messages.customclientdata.CustomClientData;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.migrations.MessageMigrationFrom17To18;
import com.diraapp.db.migrations.MessageMigrationFrom21To22;
import com.diraapp.db.migrations.MessageMigrationFrom29To30;


@Database(entities = {Message.class, Room.class, Member.class, Attachment.class,
        CustomClientData.class, MessageReading.class},
        autoMigrations = {@AutoMigration(from = 6, to = 7), @AutoMigration(from = 7, to = 8),
                @AutoMigration(from = 8, to = 9),
                @AutoMigration(from = 9, to = 10),
                @AutoMigration(from = 10, to = 11),
                @AutoMigration(from = 11, to = 12),
                @AutoMigration(from = 12, to = 13),
                @AutoMigration(from = 13, to = 14),
                @AutoMigration(from = 14, to = 15),
                @AutoMigration(from = 15, to = 16),
                @AutoMigration(from = 16, to = 17),
                @AutoMigration(from = 17, to = 18, spec = MessageMigrationFrom17To18.class),
                @AutoMigration(from = 18, to = 19),
                @AutoMigration(from = 19, to = 20),
                @AutoMigration(from = 20, to = 21),
                @AutoMigration(from = 21, to = 22, spec = MessageMigrationFrom21To22.class),
                @AutoMigration(from = 22, to = 23),
                @AutoMigration(from = 23, to = 24),
                @AutoMigration(from = 24, to = 25),
                @AutoMigration(from = 25, to = 26),
                @AutoMigration(from = 26, to = 27),
                @AutoMigration(from = 27, to = 28),
                @AutoMigration(from = 29, to = 30, spec = MessageMigrationFrom29To30.class),
                @AutoMigration(from = 30, to = 31),
                @AutoMigration(from = 31, to = 32),
        },
        version = 32,
        exportSchema = true)
@TypeConverters({CustomClientDataConverter.class,
        MessageReadingConverter.class})
public abstract class DiraMessageDatabase extends RoomDatabase {

    public static final String DB_NAME = "messages_db";
    private static DiraMessageDatabase db;

    public static DiraMessageDatabase getDatabase(Context applicationContext) {
        if (db == null) db = androidx.room.Room.databaseBuilder(applicationContext,
                        DiraMessageDatabase.class, DB_NAME)
                .addMigrations(MIGRATION_FROM_28_TO_29)
                .enableMultiInstanceInvalidation().build();
        return db;
    }

    public abstract MessageDao getMessageDao();

    public abstract AttachmentDao getAttachmentDao();
}