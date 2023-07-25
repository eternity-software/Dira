package com.diraapp.db.entities.messages;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class MessageReading {

    @PrimaryKey
    @NotNull
    private String userId;

    private long readTime;

    public MessageReading(String userId, long readTime) {
        this.userId = userId;
        this.readTime = readTime;
    }

    public String getUserId() {
        return userId;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }
}
