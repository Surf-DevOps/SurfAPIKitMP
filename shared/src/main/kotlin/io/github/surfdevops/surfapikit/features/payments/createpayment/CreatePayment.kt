package io.github.surfdevops.surfapikit.features.payments.createpayment

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

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
    val addon: Boolean? = null,
    /**
     * Recarga PROGRAMADA: dia do mês (1-31) da 1ª cobrança futura.
     * Aplicável apenas a CARD recorrente — o PG resolve o `first_charged_at` e agenda no engine.
     */
    val billingDay: Int? = null,
    /**
     * Cartão novo a tokenizar quando não há `cardId` (a cobrança futura precisa de um card_id).
     * Mutuamente exclusivo com `cardId`.
     */
    val newCard: ScheduledNewCard? = null
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

        /**
         * Recarga PROGRAMADA em cartão existente: CARD recorrente com dia da 1ª cobrança futura.
         */
        fun scheduledCardPayment(coMsisdn: Long, cardId: String, amount: Int, nuPlano: String, billingDay: Int) =
            CreatePaymentRequest(
                coMsisdn = coMsisdn, cardId = cardId, amount = amount,
                paymentType = PaymentType.CARD, nuPlano = nuPlano, isRecurring = true,
                billingDay = billingDay
            )

        /**
         * Recarga PROGRAMADA com cartão novo a tokenizar: CARD recorrente sem `cardId`.
         */
        fun scheduledCardPayment(coMsisdn: Long, newCard: ScheduledNewCard, amount: Int, nuPlano: String, billingDay: Int) =
            CreatePaymentRequest(
                coMsisdn = coMsisdn, amount = amount,
                paymentType = PaymentType.CARD, nuPlano = nuPlano, isRecurring = true,
                billingDay = billingDay, newCard = newCard
            )
    }
}

/**
 * Cartão novo a tokenizar para recarga programada (quando não há `cardId`).
 */
@Serializable
data class ScheduledNewCard(
    val holderName: String,
    val cardNumber: String,
    val holderDocument: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String
)

@Serializable
data class CreatePaymentSuccess(
    /** Não vem no corpo de sucesso da v2 (`{ message, content }`); só aparece no envelope de erro. Opcional. */
    val status: Int? = null,
    val message: String,
    val content: PaymentContent,
    /** Idem `status`: presente apenas no envelope de erro do BFF, não no sucesso. Opcional. */
    val transaction: TransactionInfo? = null
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
        /** Ausente na recarga PROGRAMADA (não há cobrança imediata). Opcional. */
        val gatewayPaymentId: String? = null,
        val productId: String,
        val msisdn: String,
        val recurrenceId: String? = null,
        val apiVersion: String,
        /** O PG declara `sourceId?` — pode não vir. Opcional. */
        val sourceId: String? = null,
        val qrCode: String? = null,
        val qrCodeUrl: String? = null,
        val pixKey: String? = null,
        val noPlano: String? = null,
        val expiresAt: String? = null,
        val expiresInSeconds: Int? = null,
        /** Recarga PROGRAMADA: data/hora da 1ª cobrança futura agendada no engine (BRT). */
        val scheduledFor: String? = null,
        /** PIX Automático: identificador da recorrência no PG. */
        val idRec: String? = null,
        /** PIX Automático: QR Code composto (jornada + cobrança). */
        val qrCodeComposto: String? = null,
        val createdAt: String
    ) {
        val isPixPayment: Boolean get() = paymentType == "PIX"
        val isCardPayment: Boolean get() = paymentType == "CARD"

        val pixInfo: Triple<String, String, String>? get() {
            if (!isPixPayment) return null
            val qr = qrCode ?: return null
            val url = qrCodeUrl ?: return null
            val key = pixKey ?: return null
            return Triple(qr, url, key)
        }

        val expirationInfo: Pair<String, Int>? get() {
            val at = expiresAt ?: return null
            val seconds = expiresInSeconds ?: return null
            return Pair(at, seconds)
        }

        val hasExpirationInfo: Boolean get() = expiresAt != null && expiresInSeconds != null
        val planName: String? get() = noPlano

        /** Recarga PROGRAMADA: houve agendamento futuro (sem cobrança imediata). */
        val isScheduled: Boolean get() = scheduledFor != null
    }

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String,
        val localTransactionId: String
    )
}

internal object CreatePaymentEndpoint : Endpoint {
    override val path = "spec-mobile/v2/payments"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createPayment(request: CreatePaymentRequest): CreatePaymentSuccess =
    client.send(CreatePaymentEndpoint, body = request)
