package net.numa08.llmdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.numa08.llmdiary.data.local.entity.PhotoEvent

@Dao
interface PhotoEventDao {
    @Insert
    suspend fun insert(event: PhotoEvent)

    @Query("SELECT * FROM photo_events WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    suspend fun getEventsForDay(startOfDay: Long, endOfDay: Long): List<PhotoEvent>
}
