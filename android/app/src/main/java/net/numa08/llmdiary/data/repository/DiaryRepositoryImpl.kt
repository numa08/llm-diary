package net.numa08.llmdiary.data.repository

import kotlinx.coroutines.flow.Flow
import net.numa08.llmdiary.data.local.dao.DiaryEntryDao
import net.numa08.llmdiary.data.local.entity.DiaryEntry
import net.numa08.llmdiary.domain.repository.DiaryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryEntryDao: DiaryEntryDao,
) : DiaryRepository {

    override fun observeAll(): Flow<List<DiaryEntry>> =
        diaryEntryDao.observeAll()

    override fun observeById(id: Long): Flow<DiaryEntry?> =
        diaryEntryDao.observeById(id)

    override suspend fun getByDate(date: String): DiaryEntry? =
        diaryEntryDao.getByDate(date)

    override suspend fun insert(entry: DiaryEntry) =
        diaryEntryDao.insert(entry)

    override fun searchByDate(query: String): Flow<List<DiaryEntry>> =
        diaryEntryDao.searchByDate(query)
}
