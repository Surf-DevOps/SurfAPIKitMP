package io.github.surfdevops.surfapikit.features.schedule.changeplan

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ChangePlanRequest(
    @SerialName("plan_id") val planId: String
)

@Serializable
data class ChangePlanSuccess(
    val status: Int,
    val message: String,
    val content: Recurrence? = null
)

internal data class ChangePlanEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/plano"
    override val method = HttpMethod.Patch
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.changeRecurrencePlan(
    request: ChangePlanRequest,
    recurrenceId: String
): ChangePlanSuccess =
    client.send(ChangePlanEndpoint(recurrenceId), body = request)
