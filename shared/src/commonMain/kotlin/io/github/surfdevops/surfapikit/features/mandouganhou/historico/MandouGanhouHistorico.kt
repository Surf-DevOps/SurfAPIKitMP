package io.github.surfdevops.surfapikit.features.mandouganhou.historico

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.mandouganhou.MandouGanhouTipo
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class MandouGanhouHistoricoRequest(
    val pagina: Int? = null,
    val limite: Int? = null,
    val tipo: MandouGanhouTipo? = null
)

@Serializable
data class MandouGanhouHistoricoSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val itens: List<HistoricoItem> = emptyList(),
        val paginacao: Paginacao? = null
    )

    @Serializable
    data class HistoricoItem(
        val resgateId: String,
        val tipo: String? = null,
        val subtipo: String? = null,
        val codigo: String? = null,
        val bonusMb: Int? = null,
        val status: String? = null,
        val protocoloProvisionamento: String? = null,
        val dataResgate: String? = null
    )

    @Serializable
    data class Paginacao(
        val pagina: Int? = null,
        val limite: Int? = null,
        val total: Int? = null,
        val totalPaginas: Int? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object MandouGanhouHistoricoEndpoint : Endpoint {
    override val path = "spec-mobile/v1/mandou-ganhou/historico"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.mandouGanhouHistorico(request: MandouGanhouHistoricoRequest = MandouGanhouHistoricoRequest()): MandouGanhouHistoricoSuccess =
    client.send(
        MandouGanhouHistoricoEndpoint,
        query = buildMap {
            request.pagina?.let { put("pagina", it) }
            request.limite?.let { put("limite", it) }
            request.tipo?.let { put("tipo", it.name) }
        }
    )
