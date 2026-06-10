package io.github.surfdevops.surfapikit.features.compreganhe.pontosavencer

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanhePontosAVencerSuccess(
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
        val vencimentos: List<Vencimento> = emptyList()
    )

    @Serializable
    data class Vencimento(
        val dataExpiracao: String? = null,
        val pontos: Int? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanhePontosAVencerEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/pontos-a-vencer"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanhePontosAVencer(periodoDias: Int? = null): CompreGanhePontosAVencerSuccess =
    client.send(
        CompreGanhePontosAVencerEndpoint,
        query = buildMap { periodoDias?.let { put("periodoDias", it) } }
    )
