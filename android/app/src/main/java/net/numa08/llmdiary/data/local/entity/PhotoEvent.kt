package net.numa08.llmdiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_events")
data class PhotoEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val uri: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
)
