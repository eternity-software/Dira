package com.diraapp.db.migrations;

import androidx.room.DeleteColumn;
import androidx.room.DeleteTable;
import androidx.room.migration.AutoMigrationSpec;

@DeleteColumn(
        tableName = "Message",
        columnName = "messageReply"
)
@DeleteTable.Entries(@DeleteTable(
        tableName = "MessageReply"
))
public class MessageMigrationFrom17To18 implements AutoMigrationSpec {
}
