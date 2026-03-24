package net.numa08.llmdiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE photo_events ADD COLUMN description TEXT DEFAULT NULL")
            }
        }
    }
    abstract fun activityEventDao(): ActivityEventDao
    abstract fun locationEventDao(): LocationEventDao
    abstract fun photoEventDao(): PhotoEventDao
    abstract fun healthDataDao(): HealthDataDao
    abstract fun diaryEntryDao(): DiaryEntryDao
}
