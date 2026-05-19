package io.github.surfdevops.surfapikit.locations

sealed class DDDResult {
    data class Found(val ddd: Int) : DDDResult()
    data object NotFound : DDDResult()
    data object PermissionDenied : DDDResult()
    data object LocationUnavailable : DDDResult()
}
