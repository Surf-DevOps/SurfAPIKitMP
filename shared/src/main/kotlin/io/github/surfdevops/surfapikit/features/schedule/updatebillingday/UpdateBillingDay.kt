package io.github.surfdevops.surfapikit.features.schedule.updatebillingday

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
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
    val content: UpdateBillingDayContent? = null
)

@Serializable
data class UpdateBillingDayContent(
    val id: String,
    val msisdn: String,
    val cpf: String,
    @SerialName("mvno_id") val mvnoId: Int,
    val ddd: String,
    @SerialName("strategy_type") val strategyType: String,
    val status: String,
    @SerialName("managed_by") val managedBy: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("card_id") val cardId: String? = null,
    @SerialName("plan_id") val planId: String,
    @SerialName("plan_name") val planName: String? = null,
    @SerialName("plan_value") val planValue: Double,
    @SerialName("plan_value_reais") val planValueReais: Double? = null,
    @SerialName("billing_day") val billingDay: Int? = null,
    @SerialName("next_execution_at") val nextExecutionAt: String,
    @SerialName("last_execution_at") val lastExecutionAt: String? = null,
    @SerialName("retry_count") val retryCount: Int,
    @SerialName("pix_pending_id") val pixPendingId: String? = null,
    @SerialName("pix_expires_at") val pixExpiresAt: String? = null,
    @SerialName("show_failure_popup") val showFailurePopup: Boolean,
    @SerialName("can_pay_via_pix") val canPayViaPix: Boolean,
    @SerialName("can_change_card") val canChangeCard: Boolean,
    @SerialName("next_card_change_available_at") val nextCardChangeAvailableAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
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
