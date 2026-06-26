package io.github.surfdevops.surfapikit.features.recharge.rechargereport

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class RechargeReportRequest(val coMsisdn: Long)

@Serializable
data class RechargeReportSuccess(
    val sucesso: Int,
    val nuTransacao: String,
    val mensagem: String,
    val resultado: Resultado? = null
) {
    @Serializable
    data class Resultado(
        val items: List<MonthGroup>,
        val janela: Janela
    )

    @Serializable
    data class MonthGroup(
        val mes: String,
        val transacoes: List<Transacao>
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
        val dtCadastro: String,
        val dtExecucao: String
    )

    @Serializable
    data class Status(
        val codigo: Int,
        val nome: String? = null
    )

    @Serializable
    data class Plano(
        val coPlano: Int,
        val noPlano: String,
        val valor: Double
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
        }
    )
