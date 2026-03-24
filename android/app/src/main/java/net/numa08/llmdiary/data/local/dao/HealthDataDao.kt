package net.numa08.llmdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.numa08.llmdiary.data.local.entity.HealthData

@Dao
interface HealthDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: HealthData)

    @Query("SELECT * FROM health_data WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: String): HealthData?
}
