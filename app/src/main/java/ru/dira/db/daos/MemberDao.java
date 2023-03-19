package ru.dira.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ru.dira.db.entities.Member;

@Dao
public interface MemberDao {

    @Insert
    void insertAll(Member... members);

    @Update
    int update(Member room);

    @Delete
    void delete(Member room);

    @Query("SELECT * FROM member WHERE id = :id AND roomSecret = :roomSecret")
    Member getMemberByIdAndRoomSecret(String id, String roomSecret);

    @Query("SELECT * FROM member WHERE roomSecret = :roomSecret")
    List<Member> getMembersByRoomSecret(String roomSecret);

    @Query("SELECT * FROM member ")
    List<Member> getAllMembers();

}
