package io.github.surfdevops.surfapikit.features.schedule.updaterecurrencecard

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class UpdateRecurrenceCardRequest(
    @SerialName("card_id") val cardId: String
)

@Serializable
data class UpdateRecurrenceCardSuccess(
    val status: Int,
    val message: String,
    val content: Recurrence? = null
)

internal data class UpdateRecurrenceCardEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/cartao"
    override val method = HttpMethod.Patch
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.updateRecurrenceCard(
    request: UpdateRecurrenceCardRequest,
    recurrenceId: String
): UpdateRecurrenceCardSuccess =
    client.send(UpdateRecurrenceCardEndpoint(recurrenceId), body = request)
