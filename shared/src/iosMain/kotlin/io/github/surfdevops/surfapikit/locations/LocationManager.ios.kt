package io.github.surfdevops.surfapikit.locations

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class LocationManager actual constructor() {

    private val manager = CLLocationManager().apply {
        desiredAccuracy = kCLLocationAccuracyHundredMeters
    }
    private val geocoder = CLGeocoder()

    actual suspend fun requestDDD(): DDDResult = suspendCancellableCoroutine { cont ->
        if (!CLLocationManager.locationServicesEnabled()) {
            cont.resume(DDDResult.LocationUnavailable); return@suspendCancellableCoroutine
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {

            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                when (manager.authorizationStatus) {
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> manager.requestLocation()
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        if (cont.isActive) cont.resume(DDDResult.PermissionDenied)
                    }
                    else -> {}
                }
            }

            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.firstOrNull() as? CLLocation
                if (location == null) {
                    if (cont.isActive) cont.resume(DDDResult.LocationUnavailable); return
                }
                geocoder.reverseGeocodeLocation(location) { placemarks, _ ->
                    val placemark = placemarks?.firstOrNull() as? platform.CoreLocation.CLPlacemark
                    val city = placemark?.locality
                    val state = placemark?.administrativeArea
                    val ddd = DDDResolver.resolve(city, state)
                    if (cont.isActive) {
                        cont.resume(if (ddd != null) DDDResult.Found(ddd) else DDDResult.NotFound)
                    }
                }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                if (cont.isActive) cont.resume(DDDResult.LocationUnavailable)
            }
        }

        manager.delegate = delegate

        when (manager.authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> manager.requestWhenInUseAuthorization()
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> manager.requestLocation()
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> cont.resume(DDDResult.PermissionDenied)
            else -> cont.resume(DDDResult.LocationUnavailable)
        }

        cont.invokeOnCancellation { manager.delegate = null }
    }
}
