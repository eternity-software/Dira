package com.diraapp;

import static com.diraapp.db.migrations.MessageMigrationFrom28to29.MIGRATION_FROM_28_TO_29;

import static org.junit.Assert.assertEquals;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.utils.Logger;
import com.google.gson.Gson;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class MessageDatabaseMigrationTest {

    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public MessageDatabaseMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                DiraMessageDatabase.class.getCanonicalName());
    }

    @Test
    public void migrateTo29() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 28);

        // message 1
        Attachment attachment = new Attachment();
        attachment.setFileUrl("fileUrl1");
        attachment.setFileCreatedTime(1);
        attachment.setFileName("file1");
        attachment.setSize(1111);
        attachment.setDisplayFileName("displayName1");
        attachment.setListened(false);
        attachment.setAttachmentType(AttachmentType.VIDEO);
        attachment.setHeight(720);
        attachment.setWidth(1280);
        attachment.setImagePreview("preview");

        ArrayList<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(attachment);
        Gson gson = new Gson();
        String attachmentJson = gson.toJson(attachmentList);

        Message message = new Message();
        message.setId("messageId1");
        message.setTime(1);

        ContentValues values = new ContentValues();
        values.put("id", message.getId());
        values.put("time", message.getTime());
        values.put("attachments", attachmentJson);

        db.insert("Message", SQLiteDatabase.CONFLICT_ABORT, values);

        // message 2
        Attachment attachment2 = new Attachment();
        attachment2.setFileName("file2");
        ArrayList<Attachment> attachmentList2 = new ArrayList<>();
        attachmentList2.add(attachment);
        String attachmentJson2 = gson.toJson(attachmentList2);

        Message message2 = new Message();
        message2.setId("messageId2");
        message2.setTime(2);

        ContentValues values2 = new ContentValues();
        values2.put("id", message2.getId());
        values2.put("time", message2.getTime());
        values2.put("attachments", attachmentJson2);

        db.insert("Message", SQLiteDatabase.CONFLICT_ABORT, values2);

        db.close();
        db = helper.runMigrationsAndValidate(TEST_DB, 29, true, MIGRATION_FROM_28_TO_29);

        Cursor cursor = db.query("SELECT * FROM 'Attachment' ORDER BY 'id'");

        cursor.moveToFirst();

        assertEquals("id1", message.getId(), cursor.getString(cursor.getColumnIndex("message_id")));
        assertEquals("file url 1", attachment.getFileUrl(), cursor.getString(cursor.getColumnIndex("fileUrl")));
        assertEquals("fileCreatedTime 1", attachment.getFileCreatedTime(), cursor.getLong(cursor.getColumnIndex("fileCreatedTime")));
        assertEquals("fileName 1", attachment.getFileName(), cursor.getString(cursor.getColumnIndex("fileName")));
        assertEquals("size 1", attachment.getSize(), cursor.getLong(cursor.getColumnIndex("size")));
        assertEquals("displayFileName 1", attachment.getDisplayFileName(), cursor.getString(cursor.getColumnIndex("displayFileName")));
        assertEquals("attachmentType 1", attachment.getAttachmentType().toString(), cursor.getString(cursor.getColumnIndex("attachmentType")));
        assertEquals("height 1", attachment.getHeight(), cursor.getInt(cursor.getColumnIndex("height")));
        assertEquals("width 1", attachment.getWidth(), cursor.getInt(cursor.getColumnIndex("width")));
        assertEquals("imagePreview 1", attachment.getImagePreview(), cursor.getString(cursor.getColumnIndex("imagePreview")));

        cursor.moveToNext();

        assertEquals("id 2", message2.getId(), cursor.getString(cursor.getColumnIndex("message_id")));
        assertEquals("fileName 2", attachment.getFileName(), cursor.getString(cursor.getColumnIndex("fileName")));

        db.close();


        Logger.logDebug("DiraTests", "MIGRATION_28_29 works correctly");

    }
}
