package com.diraapp.db.migrations;

import androidx.room.DeleteColumn;
import androidx.room.migration.AutoMigrationSpec;

@DeleteColumn(
        tableName = "Message",
        columnName = "attachments"
)
public class MessageMigrationFrom29To30 implements AutoMigrationSpec {
}
