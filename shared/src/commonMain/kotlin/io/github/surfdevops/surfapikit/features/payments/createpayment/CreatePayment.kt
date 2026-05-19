package io.github.surfdevops.surfapikit.features.payments.createpayment

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentType { @SerialName("CARD") CARD, @SerialName("PIX") PIX }

@Serializable
data class CreatePaymentRequest(
    val coMsisdn: Long,
    val cardId: String? = null,
    val amount: Int,
    val paymentType: PaymentType,
    val nuPlano: String,
    val cvv: String? = null,
    val isRecurring: Boolean? = null,
    val addon: Boolean? = null
) {
    companion object {
        fun pixPayment(coMsisdn: Long, amount: Int, nuPlano: String) = CreatePaymentRequest(
            coMsisdn = coMsisdn, amount = amount, paymentType = PaymentType.PIX, nuPlano = nuPlano
        )
        fun cardPayment(coMsisdn: Long, cardId: String, amount: Int, nuPlano: String, isRecurring: Boolean) =
            CreatePaymentRequest(
                coMsisdn = coMsisdn, cardId = cardId, amount = amount,
                paymentType = PaymentType.CARD, nuPlano = nuPlano, isRecurring = isRecurring
            )
    }
}

@Serializable
data class CreatePaymentSuccess(
    val status: Int,
    val message: String,
    val content: PaymentContent,
    val transaction: TransactionInfo
) {
    @Serializable
    data class PaymentContent(
        val id: String,
        val customerId: String,
        val cardId: String? = null,
        val amount: Int,
        val currency: String,
        val paymentType: String,
        val status: String,
        val gatewayType: String,
        val gatewayPaymentId: String,
        val productId: String,
        val msisdn: String,
        val isRecurring: Boolean,
        val sourceId: String,
        val qrCode: String? = null,
        val qrCodeUrl: String? = null,
        val pixKey: String? = null,
        val noPlano: String? = null,
        val expiresAt: String? = null,
        val expiresInSeconds: Int? = null,
        val createdAt: String
    ) {
        val isPixPayment: Boolean get() = paymentType == "PIX"
        val isCardPayment: Boolean get() = paymentType == "CARD"
        val planName: String? get() = noPlano
    }

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String,
        val localTransactionId: String
    )
}

internal object CreatePaymentEndpoint : Endpoint {
    override val path = "spec-mobile/v1/payments"
    override val method = HttpMethod.Post
}

suspend fun SurfApiKit.createPayment(request: CreatePaymentRequest): CreatePaymentSuccess =
    client.send(CreatePaymentEndpoint, body = request)
