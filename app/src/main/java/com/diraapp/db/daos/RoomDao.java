package com.diraapp.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.diraapp.db.entities.Room;

import java.util.List;

@Dao
public interface RoomDao {

    @Insert
    void insertAll(Room... rooms);

    @Update
    int update(Room room);

    @Delete
    void delete(Room room);

    @Query("SELECT * FROM room ORDER BY lastUpdatedTime DESC")
    List<Room> getAllRoomsByUpdatedTime();

    @Query("SELECT * FROM room WHERE secretName = :secretName")
    Room getRoomBySecretName(String secretName);

}