package ru.dira.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.dira.db.daos.MemberDao;
import ru.dira.db.daos.RoomDao;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;

@Database(entities = {Room.class, Message.class, Member.class}, version = 5,
        autoMigrations = {
        @AutoMigration(from = 4, to = 5)
},
        exportSchema = true)
public abstract class DiraRoomDatabase extends RoomDatabase {

    public static final String DB_NAME = "rooms_db";

    public abstract RoomDao getRoomDao();
    public abstract MemberDao getMemberDao();

    public static DiraRoomDatabase getDatabase(Context applicationContext)
    {
       return androidx.room.Room.databaseBuilder(applicationContext,
                DiraRoomDatabase.class, DB_NAME).build();
    }
}