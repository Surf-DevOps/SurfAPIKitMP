package io.github.surfdevops.surfapikit.features.schedule.paypixrecurrence

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class PayPixRecurrenceSuccess(
    val status: Int,
    val message: String,
    val content: PayPixRecurrenceContent? = null
)

@Serializable
data class PayPixRecurrenceContent(
    @SerialName("pix_id") val pixId: String,
    @SerialName("pix_code") val pixCode: String,
    @SerialName("pix_qr_url") val pixQrUrl: String,
    @SerialName("expires_at") val expiresAt: String
)

internal data class PayPixRecurrenceEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/pagar-pix"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.payPixRecurrence(recurrenceId: String): PayPixRecurrenceSuccess =
    client.send(PayPixRecurrenceEndpoint(recurrenceId))
