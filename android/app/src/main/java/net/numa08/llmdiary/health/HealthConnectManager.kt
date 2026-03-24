package net.numa08.llmdiary.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import net.numa08.llmdiary.data.local.entity.HealthData
import net.numa08.llmdiary.domain.repository.EventRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
) {
    private val client: HealthConnectClient? by lazy {
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun syncHealthDataForDate(date: LocalDate) {
        val hcClient = client ?: return
        val zone = ZoneId.systemDefault()
        val startTime = date.atStartOfDay(zone).toInstant()
        val endTime = date.plusDays(1).atStartOfDay(zone).toInstant()
        val timeRange = TimeRangeFilter.between(startTime, endTime)

        val steps = readSteps(hcClient, timeRange)
        val heartRate = readHeartRate(hcClient, timeRange)
        val sleep = readSleep(hcClient, timeRange)
        val exercise = readExercise(hcClient, timeRange)
        val weight = readWeight(hcClient, timeRange)

        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val healthData = HealthData(
            date = dateStr,
            steps = steps,
            heartRateAvg = heartRate?.first,
            heartRateMin = heartRate?.second,
            heartRateMax = heartRate?.third,
            sleepDurationMinutes = sleep,
            exerciseDurationMinutes = exercise?.first,
            exerciseType = exercise?.second,
            weightKg = weight,
        )
        eventRepository.upsertHealthData(healthData)
    }

    private suspend fun readSteps(
        client: HealthConnectClient,
        timeRange: TimeRangeFilter,
    ): Long? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(StepsRecord::class, timeRange)
            )
            response.records.sumOf { it.count }.takeIf { it > 0 }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readHeartRate(
        client: HealthConnectClient,
        timeRange: TimeRangeFilter,
    ): Triple<Double, Double, Double>? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(HeartRateRecord::class, timeRange)
            )
            val samples = response.records.flatMap { it.samples }
            if (samples.isEmpty()) return null
            val bpms = samples.map { it.beatsPerMinute.toDouble() }
            Triple(bpms.average(), bpms.min(), bpms.max())
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readSleep(
        client: HealthConnectClient,
        timeRange: TimeRangeFilter,
    ): Long? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(SleepSessionRecord::class, timeRange)
            )
            val totalMinutes = response.records.sumOf { record ->
                java.time.Duration.between(record.startTime, record.endTime).toMinutes()
            }
            totalMinutes.takeIf { it > 0 }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readExercise(
        client: HealthConnectClient,
        timeRange: TimeRangeFilter,
    ): Pair<Long, String>? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(ExerciseSessionRecord::class, timeRange)
            )
            if (response.records.isEmpty()) return null
            val totalMinutes = response.records.sumOf { record ->
                java.time.Duration.between(record.startTime, record.endTime).toMinutes()
            }
            val types = response.records.map { it.exerciseType.toString() }.distinct().joinToString(", ")
            Pair(totalMinutes, types)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readWeight(
        client: HealthConnectClient,
        timeRange: TimeRangeFilter,
    ): Double? {
        return try {
            val response = client.readRecords(
                ReadRecordsRequest(WeightRecord::class, timeRange)
            )
            response.records.lastOrNull()?.weight?.inKilograms
        } catch (_: Exception) {
            null
        }
    }
}
