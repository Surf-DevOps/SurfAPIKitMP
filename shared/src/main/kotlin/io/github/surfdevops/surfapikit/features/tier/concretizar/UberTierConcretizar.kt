package io.github.surfdevops.surfapikit.features.tier.concretizar

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/**
 * @param solicitacaoId identificador devolvido por `uberTierIntegrar` (`resultado.solicitacaoId`).
 * @param codigoAutorizacao valor do `code` na URL de callback da Uber.
 */
@Serializable
data class UberTierConcretizarRequest(
    val solicitacaoId: String,
    val codigoAutorizacao: String,
)

/** Fecha o fluxo OAuth: grava o tier do motorista e devolve o tier já registrado. */
@Serializable
data class UberTierConcretizarSuccess(
    val sucesso: Int? = null,
    val transacao: String? = null,
    val descricao: String? = null,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null,
) {
    @Serializable
    data class Resultado(
        val code: String? = null,
        val msg: String? = null,
        /** "Ouro", "Platina", "Diamante", "Azul" — nulo quando não há tier. */
        val tier: String? = null,
        val integrated: Boolean? = null,
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null,
    )
}

internal object UberTierConcretizarEndpoint : Endpoint {
    override val path = "spec-mobile/v1/tier/integracoes/uber/concretizar"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uberTierConcretizar(
    request: UberTierConcretizarRequest,
): UberTierConcretizarSuccess = client.send(UberTierConcretizarEndpoint, body = request)
