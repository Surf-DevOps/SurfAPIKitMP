package io.github.surfdevops.surfapikit.features.payments.getpayment

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.payments.createpayment.CreatePaymentSuccess
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class GetPaymentSuccess(
    val message: String,
    val content: CreatePaymentSuccess.PaymentContent? = null,
    val status: Int? = null,
    val transaction: CreatePaymentSuccess.TransactionInfo? = null
)

internal data class GetPaymentEndpoint(val paymentId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/payments/$paymentId"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPayment(paymentId: String): GetPaymentSuccess =
    client.send(GetPaymentEndpoint(paymentId))
