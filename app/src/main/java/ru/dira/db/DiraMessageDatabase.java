package ru.dira.db;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import ru.dira.db.daos.MessageDao;
import ru.dira.db.daos.RoomDao;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;


@Database(entities = {Message.class, Room.class, Member.class}, version = 5,
        autoMigrations = {
                @AutoMigration(from = 4, to = 5)
        },
        exportSchema = true)
public abstract class DiraMessageDatabase extends RoomDatabase {

    public static final String DB_NAME = "messages_db";

    public abstract MessageDao getMessageDao();

    public static DiraMessageDatabase getDatabase(Context applicationContext)
    {
        return androidx.room.Room.databaseBuilder(applicationContext,
                DiraMessageDatabase.class, DB_NAME).build();
    }
}