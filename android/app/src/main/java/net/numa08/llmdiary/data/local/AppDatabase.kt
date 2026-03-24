package net.numa08.llmdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.numa08.llmdiary.data.local.dao.ActivityEventDao
import net.numa08.llmdiary.data.local.dao.DiaryEntryDao
import net.numa08.llmdiary.data.local.dao.HealthDataDao
import net.numa08.llmdiary.data.local.dao.LocationEventDao
import net.numa08.llmdiary.data.local.dao.PhotoEventDao
import net.numa08.llmdiary.data.local.entity.ActivityEvent
import net.numa08.llmdiary.data.local.entity.DiaryEntry
import net.numa08.llmdiary.data.local.entity.HealthData
import net.numa08.llmdiary.data.local.entity.LocationEvent
import net.numa08.llmdiary.data.local.entity.PhotoEvent

@Database(
    entities = [
        ActivityEvent::class,
        LocationEvent::class,
        PhotoEvent::class,
        HealthData::class,
        DiaryEntry::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityEventDao(): ActivityEventDao
    abstract fun locationEventDao(): LocationEventDao
    abstract fun photoEventDao(): PhotoEventDao
    abstract fun healthDataDao(): HealthDataDao
    abstract fun diaryEntryDao(): DiaryEntryDao
}
