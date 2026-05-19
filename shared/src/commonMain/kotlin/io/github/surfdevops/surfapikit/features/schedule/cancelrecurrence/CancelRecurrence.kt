package io.github.surfdevops.surfapikit.features.schedule.cancelrecurrence

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod

internal data class CancelRecurrenceEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId"
    override val method = HttpMethod.Delete
}

suspend fun SurfApiKit.cancelRecurrence(recurrenceId: String) {
    client.sendVoid(CancelRecurrenceEndpoint(recurrenceId))
}
