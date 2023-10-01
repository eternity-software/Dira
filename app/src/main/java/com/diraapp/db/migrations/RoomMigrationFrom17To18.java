package com.diraapp.db.migrations;

import androidx.room.DeleteColumn;
import androidx.room.migration.AutoMigrationSpec;

@DeleteColumn(
        tableName = "Message",
        columnName = "messageReply"
)
public class RoomMigrationFrom17To18 implements AutoMigrationSpec {
}
