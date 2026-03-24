package net.numa08.llmdiary.data.repository

import net.numa08.llmdiary.data.local.dao.ActivityEventDao
import net.numa08.llmdiary.data.local.dao.HealthDataDao
import net.numa08.llmdiary.data.local.dao.LocationEventDao
import net.numa08.llmdiary.data.local.dao.PhotoEventDao
import net.numa08.llmdiary.data.local.entity.ActivityEvent
import net.numa08.llmdiary.data.local.entity.HealthData
import net.numa08.llmdiary.data.local.entity.LocationEvent
import net.numa08.llmdiary.data.local.entity.PhotoEvent
import net.numa08.llmdiary.domain.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val activityEventDao: ActivityEventDao,
    private val locationEventDao: LocationEventDao,
    private val photoEventDao: PhotoEventDao,
    private val healthDataDao: HealthDataDao,
) : EventRepository {

    override suspend fun insertActivityEvent(event: ActivityEvent) =
        activityEventDao.insert(event)

    override suspend fun insertLocationEvent(event: LocationEvent) =
        locationEventDao.insert(event)

    override suspend fun insertPhotoEvent(event: PhotoEvent) =
        photoEventDao.insert(event)

    override suspend fun getActivityEventsForDay(startOfDay: Long, endOfDay: Long) =
        activityEventDao.getEventsForDay(startOfDay, endOfDay)

    override suspend fun getLocationEventsForDay(startOfDay: Long, endOfDay: Long) =
        locationEventDao.getEventsForDay(startOfDay, endOfDay)

    override suspend fun getPhotoEventsForDay(startOfDay: Long, endOfDay: Long) =
        photoEventDao.getEventsForDay(startOfDay, endOfDay)

    override suspend fun upsertHealthData(data: HealthData) =
        healthDataDao.upsert(data)

    override suspend fun getHealthDataForDate(date: String) =
        healthDataDao.getForDate(date)
}
