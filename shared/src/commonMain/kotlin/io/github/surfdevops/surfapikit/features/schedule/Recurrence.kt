package io.github.surfdevops.surfapikit.features.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recurrence(
    val id: String,
    val msisdn: String,
    val cpf: String,
    @SerialName("mvno_id") val mvnoId: Int,
    val ddd: String,
    @SerialName("strategy_type") val strategyType: String,
    val status: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("card_id") val cardId: String? = null,
    @SerialName("plan_id") val planId: String,
    @SerialName("plan_value") val planValue: Double,
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
