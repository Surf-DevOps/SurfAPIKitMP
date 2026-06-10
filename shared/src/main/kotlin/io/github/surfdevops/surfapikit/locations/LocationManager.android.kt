package io.github.surfdevops.surfapikit.locations

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.surfdevops.surfapikit.platform.AppContextHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class LocationManager {

    suspend fun requestDDD(): DDDResult {
        val context = AppContextHolder.context

        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return DDDResult.PermissionDenied

        val location = suspendCancellableCoroutine<android.location.Location?> { cont ->
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .build()
                client.getCurrentLocation(request, null).addOnCompleteListener { task ->
                    if (cont.isActive) {
                        cont.resume(if (task.isSuccessful) task.result else null)
                    }
                }
            } catch (_: SecurityException) {
                if (cont.isActive) cont.resume(null)
            }
        } ?: return DDDResult.LocationUnavailable

        val geocoder = Geocoder(context, Locale("pt", "BR"))
        return try {
            val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<android.location.Address?> { cont ->
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { list ->
                        if (cont.isActive) cont.resume(list.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            }
            val city = address?.locality ?: address?.subAdminArea
            val state = address?.adminArea
            val ddd = DDDResolver.resolve(city, state)
            if (ddd != null) DDDResult.Found(ddd) else DDDResult.NotFound
        } catch (_: Throwable) {
            DDDResult.LocationUnavailable
        }
    }
}
