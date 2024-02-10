package com.diraapp.db.migrations;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MessageMigrationFrom28to29 extends Migration {


    public static final MessageMigrationFrom28to29 MIGRATION_FROM_28_TO_29 = new MessageMigrationFrom28to29(28, 29);

    public MessageMigrationFrom28to29(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("ALTER TABLE `Attachment` ADD COLUMN `message_id` TEXT DEFAULT ''");

        Cursor cursor = db.query("SELECT * FROM `Message` ORDER BY 'time'");

        int attachmentColumnIndex = cursor.getColumnIndex("attachments");
        int messageIdColumnIndex = cursor.getColumnIndex("id");

        while (cursor.moveToNext()) {

            String attachmentString = cursor.getString(attachmentColumnIndex);
            String messageId = cursor.getString(messageIdColumnIndex);

            ArrayList<Attachment> attachments = fromString(attachmentString);
            if (attachments.size() == 0) {
                Logger.logDebug("MessageMigration", messageId + " - 0 attachments");
                continue;
            }
            for (Attachment attachment: attachments) {
                attachment.setMessageId(messageId);
                insertAttachment(attachment, db);
                Logger.logDebug("MessageMigration", "Insert attachment " + attachment.getId() + " | " + attachment.getFileName());
            }

        }

    }

    private ArrayList<Attachment> fromString(String value) {
        Type listType = new TypeToken<ArrayList<Attachment>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    private void insertAttachment(Attachment attachment, SupportSQLiteDatabase db) {
//        String query = "INSERT OR ABORT INTO `Attachment` (`id`, " +
//                "'message_id'," +
//                "`fileUrl`,`fileCreatedTime`," +
//                "`fileName`,`size`," +
//                "`displayFileName`," +
//                "`isListened`," +
//                "`attachmentType`," +
//                "`height`,`width`,`" +
//                "imagePreview`) " +
//                "VALUES (nullif(?, 0)," +
//                "'" + attachment.getMessageId() + "' ," +
//                "'" + attachment.getFileUrl() + "' , " + attachment.getFileCreatedTime() + " ," +
//                "'" + attachment.getFileName() +  "' ," + attachment.getSize() + " ," +
//                "'" + attachment.getDisplayFileName() + "' ," +
//                attachment.isListened() +  " ," +
//                __AttachmentType_enumToString(attachment.getAttachmentType()) +  " ," +
//                attachment.getHeight() +  " ," + attachment.getWidth() + " ," +
//                attachment.getImagePreview() + ")";
//
//        db.execSQL(query);

        ContentValues values = new ContentValues();
        values.put("message_id", attachment.getMessageId());

        values.put("fileUrl", attachment.getFileUrl());
        values.put("fileCreatedTime", attachment.getFileCreatedTime());
        values.put("fileName", attachment.getFileName());
        values.put("size", attachment.getSize());
        values.put("displayFileName", attachment.getDisplayFileName());

        values.put("isListened", attachment.isListened());
        values.put("attachmentType", __AttachmentType_enumToString(attachment.getAttachmentType()));

        values.put("height", attachment.getHeight());
        values.put("width", attachment.getWidth());
        values.put("imagePreview", attachment.getImagePreview());

        db.insert("Attachment", SQLiteDatabase.CONFLICT_ABORT, values);
    }

    private String __AttachmentType_enumToString(@NonNull final AttachmentType _value) {
        switch (_value) {
            case IMAGE: return "IMAGE";
            case VOICE: return "VOICE";
            case AUDIO: return "AUDIO";
            case FILE: return "FILE";
            case VIDEO: return "VIDEO";
            case BUBBLE: return "BUBBLE";
            default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
        }
    }
}
