package net.numa08.llmdiary.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import net.numa08.llmdiary.data.local.AppDatabase
import net.numa08.llmdiary.data.local.dao.ActivityEventDao
import net.numa08.llmdiary.data.local.dao.DiaryEntryDao
import net.numa08.llmdiary.data.local.dao.HealthDataDao
import net.numa08.llmdiary.data.local.dao.LocationEventDao
import net.numa08.llmdiary.data.local.dao.PhotoEventDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // TODO: パスフレーズの安全な管理（Android Keystore等）を実装する
        val passphrase = net.sqlcipher.database.SQLiteDatabase.getBytes("changeme".toCharArray())
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "llm_diary.db",
        )
            .openHelperFactory(factory)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideActivityEventDao(db: AppDatabase): ActivityEventDao = db.activityEventDao()

    @Provides
    fun provideLocationEventDao(db: AppDatabase): LocationEventDao = db.locationEventDao()

    @Provides
    fun providePhotoEventDao(db: AppDatabase): PhotoEventDao = db.photoEventDao()

    @Provides
    fun provideHealthDataDao(db: AppDatabase): HealthDataDao = db.healthDataDao()

    @Provides
    fun provideDiaryEntryDao(db: AppDatabase): DiaryEntryDao = db.diaryEntryDao()
}
