package com.snapfilters.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.snapfilters.core.database.dao.ChatDao
import com.snapfilters.core.database.dao.LutDao
import com.snapfilters.core.database.dao.PhotoDao
import com.snapfilters.core.database.entity.ConversationEntity
import com.snapfilters.core.database.entity.CustomLutEntity
import com.snapfilters.core.database.entity.MessageEntity
import com.snapfilters.core.database.entity.PhotoRecordEntity

@Database(
    entities = [
        PhotoRecordEntity::class,
        CustomLutEntity::class,
        MessageEntity::class,
        ConversationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SnapDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun lutDao(): LutDao
    abstract fun chatDao(): ChatDao
}
