package io.github.surfdevops.surfapikit.features.customer.validatesimpurchase

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ValidateSimPurchaseRequest(
    val nuDocumento: String,
    val coMvno: Int
)

@Serializable
data class ValidateSimPurchaseSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: SimPurchaseResultado
)

@Serializable
data class SimPurchaseResultado(
    val temAmbos: Boolean,
    val esim: SimTypeInfo? = null,
    val fisico: SimTypeInfo? = null
)

@Serializable
data class SimTypeInfo(
    val chips: List<ChipInfo>,
    val pedido: PedidoInfo
)

@Serializable
data class ChipInfo(
    val nuIccid: String,
    val lpaCode: String? = null,
    val plano: PlanoInfo
)

@Serializable
data class PlanoInfo(
    val nuPlano: String,
    val noPlano: String
)

@Serializable
data class PedidoInfo(
    val numPedido: String,
    val nome: String,
    val dtPedido: String
)

internal object ValidateSimPurchaseEndpoint : Endpoint {
    override val path = "spec-mobile/v2/customer/validate-purchase"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.validateSimPurchase(request: ValidateSimPurchaseRequest): ValidateSimPurchaseSuccess =
    client.send(
        ValidateSimPurchaseEndpoint,
        query = mapOf("nuDocumento" to request.nuDocumento, "coMvno" to request.coMvno.toString())
    )
