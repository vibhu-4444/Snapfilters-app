package com.snapfilters.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_luts")
data class CustomLutEntity(
    @PrimaryKey val id: String, // sha256 prefix
    val title: String,
    val size: Int, // 17, 33, or 65
    val filePath: String,
    val byteSize: Long,
    val createdAt: Long = System.currentTimeMillis()
)
