package com.diraapp.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Member {

    @PrimaryKey
    @NotNull
    private String idPlusSecret;

    private String id;

    private String nickname;
    private String imagePath;
    private String roomSecret;
    private long lastTimeUpdated;

    public Member(@NotNull String id, String nickname, String imagePath, String roomSecret, long lastTimeUpdated) {
        this.id = id;
        this.nickname = nickname;
        this.imagePath = imagePath;
        idPlusSecret = id + "_" + roomSecret;
        this.roomSecret = roomSecret;
        this.lastTimeUpdated = lastTimeUpdated;
    }


    @NotNull
    public String getIdPlusSecret() {
        return idPlusSecret;
    }

    public void setIdPlusSecret(@NotNull String idPlusSecret) {
        this.idPlusSecret = idPlusSecret;
    }

    public long getLastTimeUpdated() {
        return lastTimeUpdated;
    }

    public void setLastTimeUpdated(long lastTimeUpdated) {
        this.lastTimeUpdated = lastTimeUpdated;
    }

    public String getRoomSecret() {
        return roomSecret;
    }

    public void setRoomSecret(String roomSecret) {
        this.roomSecret = roomSecret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
