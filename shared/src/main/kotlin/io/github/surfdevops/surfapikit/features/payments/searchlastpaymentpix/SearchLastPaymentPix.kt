package io.github.surfdevops.surfapikit.features.payments.searchlastpaymentpix

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class SearchLastPaymentPixSuccess(
    val status: Int,
    val message: String,
    val content: PixPendingContent,
    val transaction: TransactionInfo
) {
    @Serializable
    data class PixPendingContent(
        val id: String,
        val customerId: String,
        val amount: Int,
        val currency: String,
        val paymentType: String,
        val status: String,
        val gatewayType: String,
        val gatewayPaymentId: String,
        val productId: String,
        val noPlano: String,
        val msisdn: String,
        val metadata: PixMetadata,
        val expiresAt: String,
        val expiresInSeconds: Int,
        val createdAt: String,
        val updatedAt: String
    )

    @Serializable
    data class PixMetadata(
        val ddd: String,
        val mvno: Int,
        val noPlano: String,
        val pixExpirationDate: String
    )

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String,
        val localTransactionId: String
    )
}

internal data class SearchLastPaymentPixEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/payments/pix/pending/by-co-msisdn/$coMsisdn"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.searchLastPaymentPix(coMsisdn: String): SearchLastPaymentPixSuccess =
    client.send(SearchLastPaymentPixEndpoint(coMsisdn))
