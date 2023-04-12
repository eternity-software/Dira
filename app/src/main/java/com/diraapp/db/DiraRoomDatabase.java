package com.diraapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;

@Database(entities = {Room.class, Message.class, Member.class}, version = 6,
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