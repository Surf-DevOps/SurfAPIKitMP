package io.github.surfdevops.surfapikit.features.schedule.cancelrecurrence

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

internal data class CancelRecurrenceEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId"
    override val method = HttpMethod.Delete
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.cancelRecurrence(recurrenceId: String) {
    client.sendVoid(CancelRecurrenceEndpoint(recurrenceId))
}
