package com.diraapp.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.diraapp.api.processors.UpdateProcessor;

import org.jetbrains.annotations.NotNull;

@Entity
public class Room {

    @PrimaryKey
    @NotNull
    private String secretName;

    private String name;
    private String lastMessageId;
    private String imagePath;
    @ColumnInfo(defaultValue = UpdateProcessor.OFFICIAL_ADDRESS)
    private String serverAddress;

    private long lastUpdatedTime;

    @ColumnInfo(defaultValue = "true")
    private boolean updatedRead;

    private long lastUpdateId;
    private long timeServerStartup;

    @Ignore
    private Message message;

    public Room(String name, long lastUpdatedTime, String secretName, String serverAddress) {
        this.name = name;
        this.lastUpdatedTime = lastUpdatedTime;
        this.secretName = secretName;
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        if (serverAddress == null) {
            serverAddress = UpdateProcessor.OFFICIAL_ADDRESS;
        }
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
