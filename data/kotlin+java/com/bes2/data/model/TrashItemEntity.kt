package com.bes2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trash_items",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["status"])
    ]
)
data class TrashItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val filePath: String,
    val timestamp: Long,
    val size: Long = 0,
    
    // Status: READY (Detected), KEPT (User kept), DELETED (User deleted)
    val status: String = "READY"
)
