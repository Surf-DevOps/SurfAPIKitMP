package io.github.surfdevops.surfapikit.features.compreganhe.resgate

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanheResgateRequest(
    val planoBonusId: Int,
    val msisdnDestino: String? = null,
    val idempotencyKey: String? = null
)

@Serializable
data class CompreGanheResgateSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val resgateId: String,
        val status: String? = null,
        val idempotencyKey: String? = null,
        val plano: Plano? = null,
        val pontosDebitados: Int? = null,
        val saldoRestante: Int? = null,
        val msisdnDestino: String? = null,
        val protocoloProvisionamento: String? = null,
        val dataResgate: String? = null
    )

    @Serializable
    data class Plano(
        val id: String,
        val codigoPlano: String? = null,
        val nome: String? = null,
        val addonTipo: String? = null,
        val addonQuantidade: Int? = null,
        val validadeDias: Int? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanheResgateEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/resgate"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheResgatar(request: CompreGanheResgateRequest): CompreGanheResgateSuccess =
    client.send(CompreGanheResgateEndpoint, body = request)
