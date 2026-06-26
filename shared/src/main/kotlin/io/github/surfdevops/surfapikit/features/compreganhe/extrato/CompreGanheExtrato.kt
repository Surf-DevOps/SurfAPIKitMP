package io.github.surfdevops.surfapikit.features.compreganhe.extrato

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanheExtratoSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val saldoAtual: Int? = null,
        val periodoDias: Int? = null,
        val movimentacoes: List<Movimentacao> = emptyList()
    )

    @Serializable
    data class Movimentacao(
        val tipo: String,
        val data: String? = null,
        val pontos: Int? = null,
        val descricao: String? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanheExtratoEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/extrato"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheExtrato(periodoDias: Int? = null): CompreGanheExtratoSuccess =
    client.send(
        CompreGanheExtratoEndpoint,
        query = buildMap { periodoDias?.let { put("periodoDias", it) } }
    )
