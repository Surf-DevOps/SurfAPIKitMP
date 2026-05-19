package io.github.surfdevops.surfapikit.locations

expect class LocationManager() {
    suspend fun requestDDD(): DDDResult
}
