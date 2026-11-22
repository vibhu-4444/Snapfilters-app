package com.snapfilters.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snapfilters.core.database.entity.CustomLutEntity
import com.snapfilters.core.database.entity.PhotoRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PhotoRecordEntity)

    @Query("SELECT * FROM photo_records WHERE captureId = :captureId")
    suspend fun getRecordById(captureId: String): PhotoRecordEntity?

    @Query("SELECT * FROM photo_records ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<PhotoRecordEntity>>

    @Query("SELECT COUNT(*) FROM photo_records WHERE filterId = :filterId")
    suspend fun countPhotosUsingFilter(filterId: String): Int

    @Query("DELETE FROM photo_records WHERE captureId = :captureId")
    suspend fun deleteRecord(captureId: String)
}

@Dao
interface LutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLut(lut: CustomLutEntity)

    @Query("SELECT * FROM custom_luts ORDER BY createdAt DESC")
    fun getAllLuts(): Flow<List<CustomLutEntity>>

    @Query("SELECT * FROM custom_luts WHERE id = :id")
    suspend fun getLutById(id: String): CustomLutEntity?

    @Query("DELETE FROM custom_luts WHERE id = :id")
    suspend fun deleteLut(id: String)
}
