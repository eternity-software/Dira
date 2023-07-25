package com.diraapp.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.DeleteColumn;
import androidx.room.RoomDatabase;

import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.Room;

@Database(entities = {Room.class, Message.class, Member.class},
        autoMigrations = {@AutoMigration(from = 6, to = 7),
                @AutoMigration(from = 7, to = 8),
                @AutoMigration(from = 8, to = 9),
                @AutoMigration(from = 9, to = 10),
                @AutoMigration(from = 10, to = 11),
                @AutoMigration(from = 11, to = 12),
                @AutoMigration(from = 12, to = 13)},
        version = 13,
        exportSchema = true)
public abstract class DiraRoomDatabase extends RoomDatabase {

    public static final String DB_NAME = "rooms_db";

    public static DiraRoomDatabase getDatabase(Context applicationContext) {
        return androidx.room.Room.databaseBuilder(applicationContext,
                DiraRoomDatabase.class, DB_NAME).build();
    }

    public abstract RoomDao getRoomDao();

    public abstract MemberDao getMemberDao();
}