package io.github.surfdevops.surfapikit.features.coupons.validatecoupon

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ValidateCouponRequest(
    val idCupom: String,
    val coMvno: Int,
    val nuPlano: String,
    val vlRecarga: Double,
    val nuDocumento: String
)

@Serializable
data class ValidateCouponSuccess(
    val status: Int,
    val message: String,
    val content: CouponContent,
    val transaction: TransactionInfo? = null
) {
    @Serializable
    data class CouponContent(
        val valido: Boolean,
        val idCupom: String,
        val tpDesconto: DiscountType,
        val vlRecarga: Double,
        val vlDescontoAplicado: Double,
        val vlFinal: Double
    )

    @Serializable
    enum class DiscountType {
        @SerialName("PERCENTUAL") PERCENTUAL,
        @SerialName("FIXO") FIXO
    }

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null
    )
}

internal object ValidateCouponEndpoint : Endpoint {
    override val path = "spec-mobile/v2/payments/cupom/validar"
    override val method = HttpMethod.Post
}

/**
 * Valida um cupom de desconto para uma recarga.
 * Esta chamada é apenas um preview e não reserva o cupom.
 */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.validateCoupon(request: ValidateCouponRequest): ValidateCouponSuccess =
    client.send(ValidateCouponEndpoint, body = request)
