package net.numa08.llmdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_events")
data class ActivityEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val activityType: Int,
    val confidence: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
)
