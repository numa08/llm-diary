package net.numa08.llmdiary.sensor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.numa08.llmdiary.data.local.entity.ActivityEvent
import net.numa08.llmdiary.domain.repository.EventRepository
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ActivityRecognitionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventRepository: EventRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityRecognitionResult.hasResult(intent)) return
        val result = ActivityRecognitionResult.extractResult(intent) ?: return
        val activity = result.mostProbableActivity

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val (lat, lon, address) = resolveLocation(context)
                val event = ActivityEvent(
                    timestamp = result.time,
                    activityType = activity.type,
                    confidence = activity.confidence,
                    latitude = lat,
                    longitude = lon,
                    address = address,
                )
                eventRepository.insertActivityEvent(event)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun resolveLocation(context: Context): Triple<Double?, Double?, String?> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Triple(null, null, null)
        }

        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationTask = locationClient.lastLocation
        val location = try {
            com.google.android.gms.tasks.Tasks.await(locationTask)
        } catch (_: Exception) {
            null
        } ?: return Triple(null, null, null)

        val address = try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var result: String? = null
                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                    result = addresses.firstOrNull()?.let { addr ->
                        (0..addr.maxAddressLineIndex).joinToString("\n") { addr.getAddressLine(it) }
                    }
                }
                result
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.firstOrNull()?.let { addr ->
                        (0..addr.maxAddressLineIndex).joinToString("\n") { addr.getAddressLine(it) }
                    }
            }
        } catch (_: Exception) {
            null
        }

        return Triple(location.latitude, location.longitude, address)
    }
}
