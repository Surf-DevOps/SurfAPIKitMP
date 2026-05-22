package io.github.surfdevops.surfapikit.features.schedule.updatebillingday

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class UpdateBillingDayRequest(
    @SerialName("billing_day") val billingDay: Int
)

@Serializable
data class UpdateBillingDaySuccess(
    val status: Int,
    val message: String,
    val content: Recurrence? = null
)

internal data class UpdateBillingDayEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/dia-cobranca"
    override val method = HttpMethod.Patch
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.updateBillingDay(
    request: UpdateBillingDayRequest,
    recurrenceId: String
): UpdateBillingDaySuccess =
    client.send(UpdateBillingDayEndpoint(recurrenceId), body = request)
