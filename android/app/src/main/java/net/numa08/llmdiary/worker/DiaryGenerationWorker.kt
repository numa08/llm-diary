package net.numa08.llmdiary.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.numa08.llmdiary.data.local.entity.DiaryEntry
import net.numa08.llmdiary.domain.repository.DiaryRepository
import net.numa08.llmdiary.domain.repository.EventRepository
import net.numa08.llmdiary.health.HealthConnectManager
import net.numa08.llmdiary.llm.DiaryGenerator
import net.numa08.llmdiary.llm.ImageDescriber
import net.numa08.llmdiary.llm.LlmEngine
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltWorker
class DiaryGenerationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepository: EventRepository,
    private val diaryRepository: DiaryRepository,
    private val healthConnectManager: HealthConnectManager,
    private val diaryGenerator: DiaryGenerator,
    private val llmEngine: LlmEngine,
    private val imageDescriber: ImageDescriber,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val yesterday = LocalDate.now().minusDays(1)
        val dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Skip if diary already exists for this date
        if (diaryRepository.getByDate(dateStr) != null) {
            return Result.success()
        }

        return try {
            // Sync health data first
            healthConnectManager.syncHealthDataForDate(yesterday)

            // Get time range for yesterday
            val zone = ZoneId.systemDefault()
            val startOfDay = yesterday.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDay = yesterday.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

            // Retry image description for photos that failed earlier
            describeUndescribedPhotos()

            // Collect all events
            val activities = eventRepository.getActivityEventsForDay(startOfDay, endOfDay)
            val locations = eventRepository.getLocationEventsForDay(startOfDay, endOfDay)
            val photos = eventRepository.getPhotoEventsForDay(startOfDay, endOfDay)
            val healthData = eventRepository.getHealthDataForDate(dateStr)

            // Load model and generate diary
            val modelPath = inputData.getString(KEY_MODEL_PATH) ?: return Result.failure()
            if (!llmEngine.isModelLoaded()) {
                llmEngine.loadModel(modelPath)
            }

            val content = diaryGenerator.generate(
                activities = activities,
                locations = locations,
                photos = photos,
                healthData = healthData,
                date = dateStr,
            )

            // Save diary entry
            val entry = DiaryEntry(
                date = dateStr,
                content = content,
                createdAt = System.currentTimeMillis(),
            )
            diaryRepository.insert(entry)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Diary generation failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    /**
     * 説明が未生成の写真に対して Gemini Nano で再試行する。
     */
    private suspend fun describeUndescribedPhotos() {
        val undescribed = eventRepository.getPhotoEventsWithoutDescription()
        if (undescribed.isEmpty()) return

        Log.i(TAG, "Retrying image description for ${undescribed.size} photos")
        for (photo in undescribed) {
            val description = imageDescriber.describe(photo.uri)
            if (description != null) {
                eventRepository.updatePhotoDescription(photo.id, description)
            }
        }
    }

    companion object {
        private const val TAG = "DiaryGenerationWorker"
        const val KEY_MODEL_PATH = "model_path"
        const val WORK_NAME = "diary_generation"
    }
}
