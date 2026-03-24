package net.numa08.llmdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.numa08.llmdiary.data.local.entity.ActivityEvent

@Dao
interface ActivityEventDao {
    @Insert
    suspend fun insert(event: ActivityEvent)

    @Query("SELECT * FROM activity_events WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    suspend fun getEventsForDay(startOfDay: Long, endOfDay: Long): List<ActivityEvent>

    @Query("SELECT * FROM activity_events WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    fun observeEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<ActivityEvent>>
}
