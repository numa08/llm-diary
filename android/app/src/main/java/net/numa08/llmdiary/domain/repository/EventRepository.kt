package net.numa08.llmdiary.domain.repository

import net.numa08.llmdiary.data.local.entity.ActivityEvent
import net.numa08.llmdiary.data.local.entity.HealthData
import net.numa08.llmdiary.data.local.entity.LocationEvent
import net.numa08.llmdiary.data.local.entity.PhotoEvent

interface EventRepository {
    suspend fun insertActivityEvent(event: ActivityEvent)
    suspend fun insertLocationEvent(event: LocationEvent)
    suspend fun insertPhotoEvent(event: PhotoEvent): Long
    suspend fun updatePhotoDescription(id: Long, description: String)
    suspend fun getPhotoEventsWithoutDescription(): List<PhotoEvent>
    suspend fun getActivityEventsForDay(startOfDay: Long, endOfDay: Long): List<ActivityEvent>
    suspend fun getLocationEventsForDay(startOfDay: Long, endOfDay: Long): List<LocationEvent>
    suspend fun getPhotoEventsForDay(startOfDay: Long, endOfDay: Long): List<PhotoEvent>
    suspend fun upsertHealthData(data: HealthData)
    suspend fun getHealthDataForDate(date: String): HealthData?
}
