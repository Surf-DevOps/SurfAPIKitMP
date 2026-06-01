package io.github.surfdevops.surfapikit.features.recharge.rechargereport

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class RechargeReportRequest(val coMsisdn: String, val meses: Int? = null)

@Serializable
data class RechargeReportSuccess(
    val sucesso: Int,
    val nuTransacao: String,
    val mensagem: String,
    val resultado: Resultado? = null
) {
    @Serializable
    data class Resultado(
        val items: List<MonthGroup> = emptyList(),
        val janela: Janela? = null
    )

    @Serializable
    data class MonthGroup(
        val mes: String,
        val transacoes: List<Transacao> = emptyList()
    )

    @Serializable
    data class Transacao(
        val tipo: String,
        val id: String,
        val nuTransacao: String,
        val vlCredito: Double,
        val status: Status,
        val noProduto: String? = null,
        val origem: String? = null,
        val plano: Plano? = null,
        val dtCadastro: String? = null,
        val dtExecucao: String? = null
    )

    @Serializable
    data class Status(
        val codigo: Int,
        val nome: String? = null
    )

    @Serializable
    data class Plano(
        val coPlano: Int? = null,
        val noPlano: String? = null,
        val valor: Double? = null
    )

    @Serializable
    data class Janela(val meses: Int)
}

internal object RechargeReportEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recarga/relatorio"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.rechargeReport(request: RechargeReportRequest): RechargeReportSuccess =
    client.send(
        RechargeReportEndpoint,
        query = buildMap {
            put("coMsisdn", request.coMsisdn)
            request.meses?.let { put("meses", it) }
        }
    )
