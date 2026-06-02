package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_items")
data class StorageItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "PHOTO", "VIDEO", "JUNK", "DOWNLOAD"
    val sizeBytes: Long,
    val addedTime: Long = System.currentTimeMillis(),
    val isOffloaded: Boolean = false,
    val isDeleted: Boolean = false,
    val groupKey: String? = null, // To group duplicate photos together
    val fileCount: Int = 1 // To represent multiple items bundled or just singular
)
