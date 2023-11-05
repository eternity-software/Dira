package com.diraapp.db.migrations;

import androidx.room.RenameColumn;
import androidx.room.migration.AutoMigrationSpec;

@RenameColumn.Entries(
        @RenameColumn(
                tableName = "Attachment",
                fromColumnName = "realFileName",
                toColumnName = "displayFileName"
        )
)
public class MessageMigrationFrom21To22 implements AutoMigrationSpec {
}
