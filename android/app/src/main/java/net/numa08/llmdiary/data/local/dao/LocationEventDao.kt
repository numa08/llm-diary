package net.numa08.llmdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.numa08.llmdiary.data.local.entity.LocationEvent

@Dao
interface LocationEventDao {
    @Insert
    suspend fun insert(event: LocationEvent)

    @Query("SELECT * FROM location_events WHERE timestamp BETWEEN :startOfDay AND :endOfDay ORDER BY timestamp ASC")
    suspend fun getEventsForDay(startOfDay: Long, endOfDay: Long): List<LocationEvent>
}
