package net.numa08.llmdiary.domain.repository

import kotlinx.coroutines.flow.Flow
import net.numa08.llmdiary.data.local.entity.DiaryEntry

interface DiaryRepository {
    fun observeAll(): Flow<List<DiaryEntry>>
    fun observeById(id: Long): Flow<DiaryEntry?>
    suspend fun getByDate(date: String): DiaryEntry?
    suspend fun insert(entry: DiaryEntry)
    fun searchByDate(query: String): Flow<List<DiaryEntry>>
}
