package com.diraapp.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.diraapp.db.converters.UnreadIdsConverter;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.migrations.RoomMigrationFrom17To18;
import com.diraapp.db.migrations.RoomMigrationFrom21To22;
import com.diraapp.db.migrations.RoomMigrationFrom29To30;

@Database(entities = {Room.class, Message.class, Member.class},
        autoMigrations = {@AutoMigration(from = 6, to = 7),
                @AutoMigration(from = 7, to = 8),
                @AutoMigration(from = 8, to = 9),
                @AutoMigration(from = 9, to = 10),
                @AutoMigration(from = 10, to = 11),
                @AutoMigration(from = 11, to = 12),
                @AutoMigration(from = 12, to = 13),
                @AutoMigration(from = 13, to = 14),
                @AutoMigration(from = 14, to = 15),
                @AutoMigration(from = 15, to = 16),
                @AutoMigration(from = 16, to = 17),
                @AutoMigration(from = 17, to = 18, spec = RoomMigrationFrom17To18.class),
                @AutoMigration(from = 18, to = 19),
                @AutoMigration(from = 19, to = 20),
                @AutoMigration(from = 20, to = 21),
                @AutoMigration(from = 21, to = 22, spec = RoomMigrationFrom21To22.class),
                @AutoMigration(from = 22, to = 23),
                @AutoMigration(from = 23, to = 24),
                @AutoMigration(from = 24, to = 25),
                @AutoMigration(from = 25, to = 26),
                @AutoMigration(from = 26, to = 27),
                @AutoMigration(from = 27, to = 28),
                @AutoMigration(from = 28, to = 29),
                @AutoMigration(from = 29, to = 30, spec = RoomMigrationFrom29To30.class),
                @AutoMigration(from = 30, to = 31),
                @AutoMigration(from = 31, to = 32),
        },
        version = 32,
        exportSchema = true)
@TypeConverters({UnreadIdsConverter.class})
public abstract class DiraRoomDatabase extends RoomDatabase {

    public static final String DB_NAME = "rooms_db";
    public static DiraRoomDatabase db;

    public static DiraRoomDatabase getDatabase(Context applicationContext) {
        if (db == null) db = androidx.room.Room.databaseBuilder(applicationContext,
                DiraRoomDatabase.class, DB_NAME).enableMultiInstanceInvalidation().build();
        return db;
    }

    public abstract RoomDao getRoomDao();

    public abstract MemberDao getMemberDao();
}