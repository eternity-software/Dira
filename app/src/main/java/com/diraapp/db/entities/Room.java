package com.diraapp.db.entities;

import androidx.annotation.ColorInt;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.updates.Update;
import com.diraapp.db.converters.UnreadIdsConverter;
import com.diraapp.db.entities.messages.Message;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Entity
@TypeConverters({UnreadIdsConverter.class})
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
    @ColumnInfo(defaultValue = "0")
    private long timeEncryptionKeyUpdated;
    @ColumnInfo(defaultValue = "")
    private String firstVisibleScrolledItemId;
    @ColumnInfo(defaultValue = ("" + Update.DEFAULT_UPDATE_EXPIRE_SEC))
    private int updateExpireSec;
    @ColumnInfo(defaultValue = "")
    private String encryptionKey;
    @ColumnInfo(defaultValue = "0")
    private String clientSecret;

    @ColumnInfo(defaultValue = "true")
    private boolean isNotificationsEnabled;

    @ColumnInfo(defaultValue = "")
    private String unsentText;

    private ArrayList<String> unreadMessagesIds = new ArrayList<>();

    @Ignore
    private Message message;

    public Room(String name, long lastUpdatedTime, String secretName, String serverAddress,
                boolean isNotificationsEnabled, ArrayList<String> unreadMessagesIds) {
        this.name = name;
        this.lastUpdatedTime = lastUpdatedTime;
        this.secretName = secretName;
        this.serverAddress = serverAddress;
        this.isNotificationsEnabled = isNotificationsEnabled;
        if (unreadMessagesIds == null) {
            this.unreadMessagesIds = new ArrayList<>();
            return;
        }
        this.unreadMessagesIds = unreadMessagesIds;
    }

    public int getUpdateExpireSec() {
        return updateExpireSec;
    }

    public void setUpdateExpireSec(int updateExpireSec) {
        this.updateExpireSec = updateExpireSec;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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

    public long getTimeEncryptionKeyUpdated() {
        return timeEncryptionKeyUpdated;
    }

    public void setTimeEncryptionKeyUpdated(long timeEncryptionKeyUpdated) {
        this.timeEncryptionKeyUpdated = timeEncryptionKeyUpdated;
    }

    public String getEncryptionKey() {
        if (encryptionKey == null) encryptionKey = "";
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
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

    public boolean isNotificationsEnabled() {
        return isNotificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        isNotificationsEnabled = notificationsEnabled;
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

    public ArrayList<String> getUnreadMessagesIds() {
        return unreadMessagesIds;
    }

    public void setUnreadMessagesIds(ArrayList<String> unreadMessagesIds) {
        this.unreadMessagesIds = unreadMessagesIds;
    }

    public boolean removeFromUnreadMessages(ArrayList<String> list) {
        return unreadMessagesIds.removeAll(list);
    }

    public void addNewUnreadMessageId(String id) {
        if (unreadMessagesIds == null) {
            unreadMessagesIds = new ArrayList<>();
        }
        unreadMessagesIds.add(id);
    }

    public String getUnsentText() {
        return unsentText;
    }

    public void setUnsentText(String unsentText) {
        this.unsentText = unsentText;
    }

    public String getFirstVisibleScrolledItemId() {
        if(firstVisibleScrolledItemId == null) firstVisibleScrolledItemId = "null";
        return firstVisibleScrolledItemId;
    }

    public void setFirstVisibleScrolledItemId(String firstVisibleScrolledItemId) {
        this.firstVisibleScrolledItemId = firstVisibleScrolledItemId;
    }
}
