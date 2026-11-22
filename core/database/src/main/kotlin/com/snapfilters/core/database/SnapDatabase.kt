package com.snapfilters.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.snapfilters.core.database.dao.LutDao
import com.snapfilters.core.database.dao.PhotoDao
import com.snapfilters.core.database.entity.CustomLutEntity
import com.snapfilters.core.database.entity.PhotoRecordEntity

@Database(
    entities = [PhotoRecordEntity::class, CustomLutEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SnapDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun lutDao(): LutDao
}
