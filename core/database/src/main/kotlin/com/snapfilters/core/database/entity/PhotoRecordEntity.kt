package com.snapfilters.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SaveMode {
    ORIGINAL_ONLY,
    FILTERED_ONLY,
    BOTH
}

@Entity(tableName = "photo_records")
data class PhotoRecordEntity(
    @PrimaryKey val captureId: String,
    val mode: SaveMode,
    val originalUri: String?,
    val editedUri: String?,
    val filterId: String?,
    val intensity: Float,
    val createdAt: Long = System.currentTimeMillis()
)
