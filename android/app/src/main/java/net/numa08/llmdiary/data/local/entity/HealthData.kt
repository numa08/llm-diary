package net.numa08.llmdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val steps: Long? = null,
    val heartRateAvg: Double? = null,
    val heartRateMin: Double? = null,
    val heartRateMax: Double? = null,
    val sleepDurationMinutes: Long? = null,
    val exerciseDurationMinutes: Long? = null,
    val exerciseType: String? = null,
    val weightKg: Double? = null,
)
