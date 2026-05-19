package io.github.surfdevops.surfapikit.features.schedule.getrecurrence

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class GetRecurrenceSuccess(
    val status: Int,
    val message: String,
    val content: Recurrence? = null
)

internal data class GetRecurrenceEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId"
    override val method = HttpMethod.Get
}

suspend fun SurfApiKit.getRecurrence(recurrenceId: String): GetRecurrenceSuccess =
    client.send(GetRecurrenceEndpoint(recurrenceId))
