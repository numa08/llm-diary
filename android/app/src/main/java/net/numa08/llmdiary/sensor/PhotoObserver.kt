package net.numa08.llmdiary.sensor

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.numa08.llmdiary.data.local.entity.PhotoEvent
import net.numa08.llmdiary.domain.repository.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            uri ?: return
            scope.launch { handleNewPhoto(uri) }
        }
    }

    fun startObserving() {
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer,
        )
    }

    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(observer)
    }

    private suspend fun handleNewPhoto(uri: Uri) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
        )

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dateAdded = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                )

                @Suppress("DEPRECATION")
                val lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE)
                )

                @Suppress("DEPRECATION")
                val lon = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE)
                )

                val event = PhotoEvent(
                    timestamp = dateAdded * 1000,
                    uri = uri.toString(),
                    latitude = if (lat != 0.0) lat else null,
                    longitude = if (lon != 0.0) lon else null,
                )
                eventRepository.insertPhotoEvent(event)
            }
        }
    }
}
