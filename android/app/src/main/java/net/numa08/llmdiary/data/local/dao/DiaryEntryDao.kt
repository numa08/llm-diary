package net.numa08.llmdiary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.numa08.llmdiary.data.local.entity.DiaryEntry

@Dao
interface DiaryEntryDao {
    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun observeById(id: Long): Flow<DiaryEntry?>

    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE date LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchByDate(query: String): Flow<List<DiaryEntry>>
}
