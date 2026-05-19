package io.github.surfdevops.surfapikit.features.schedule.getrecurrenceevents

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetRecurrenceEventsSuccess(
    val status: Int,
    val message: String,
    val content: List<RecurrenceEvent>? = null
)

@Serializable
data class RecurrenceEvent(
    val id: String,
    @SerialName("recurrence_id") val recurrenceId: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("actor_type") val actorType: String,
    @SerialName("actor_id") val actorId: String? = null,
    val description: String,
    @SerialName("created_at") val createdAt: String
)

internal data class GetRecurrenceEventsEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/eventos"
    override val method = HttpMethod.Get
}

suspend fun SurfApiKit.getRecurrenceEvents(
    recurrenceId: String,
    limit: Int = 10,
    offset: Int = 0
): GetRecurrenceEventsSuccess =
    client.send(
        GetRecurrenceEventsEndpoint(recurrenceId),
        query = mapOf("limit" to limit.toString(), "offset" to offset.toString())
    )
