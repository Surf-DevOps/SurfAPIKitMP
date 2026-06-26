package io.github.surfdevops.surfapikit.features.compreganhe.saldo

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class CompreGanheSaldoRequest(
    val aba: String? = null,
    val pagina: Int? = null,
    val limite: Int? = null
)

@Serializable
data class CompreGanheSaldoSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val saldoAcumulado: Int? = null,
        val limiteMensalDisponivel: Int? = null,
        val alertaExpiracao: AlertaExpiracao? = null,
        val cupons: List<Cupom> = emptyList(),
        val paginacao: Paginacao? = null
    )

    @Serializable
    data class AlertaExpiracao(
        val pontos: Int? = null,
        val diasRestantes: Int? = null,
        val dataExpiracao: String? = null
    )

    @Serializable
    data class Cupom(
        val id: String,
        val cnpjEmissor: String? = null,
        val valorTotal: Double? = null,
        val pontos: Int? = null,
        val validade: String? = null,
        val dataEscaneamento: String? = null,
        val status: String? = null
    )

    @Serializable
    data class Paginacao(
        val totalItens: Int? = null,
        val totalPaginas: Int? = null,
        val paginaAtual: Int? = null,
        val limite: Int? = null,
        val temProxima: Boolean? = null,
        val temAnterior: Boolean? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanheSaldoEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/saldo"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheSaldo(request: CompreGanheSaldoRequest = CompreGanheSaldoRequest()): CompreGanheSaldoSuccess =
    client.send(
        CompreGanheSaldoEndpoint,
        query = buildMap {
            request.aba?.let { put("aba", it) }
            request.pagina?.let { put("pagina", it) }
            request.limite?.let { put("limite", it) }
        }
    )
