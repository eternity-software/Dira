package ru.dira.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class Room {

    @PrimaryKey
    @NotNull
    private String secretName;

    private String name;
    private String lastMessageId;
    private String imagePath;

    private long lastUpdatedTime;

    @ColumnInfo(defaultValue = "true")
    private boolean updatedRead;

    private long lastUpdateId;
    private long timeServerStartup;

    @Ignore
    private Message message;


    public Room(String name, long lastUpdatedTime, String secretName) {
        this.name = name;
        this.lastUpdatedTime = lastUpdatedTime;
        this.secretName = secretName;
    }

    public boolean isUpdatedRead() {
        return updatedRead;
    }

    public void setUpdatedRead(boolean updatedRead) {
        this.updatedRead = updatedRead;
    }

    public long getTimeServerStartup() {
        return timeServerStartup;
    }

    public void setTimeServerStartup(long timeServerStartup) {
        this.timeServerStartup = timeServerStartup;
    }

    public Message getMessage() {
        return message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public long getLastUpdateId() {
        return lastUpdateId;
    }

    public void setLastUpdateId(long lastUpdateId) {
        this.lastUpdateId = lastUpdateId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }
}
