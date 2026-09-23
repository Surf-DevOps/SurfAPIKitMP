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
        val dtExecucao: String,
        /**
         * Quebra bruto/desconto/líquido da recarga. Só a v2 (`rechargeReportV2`) devolve
         * este bloco, e mesmo lá vem `null` quando a recarga não teve cupom — que é o caso
         * comum. O BSS não guarda o que o cliente pagou; isto vem do payment-gateway.
         */
        val desconto: Desconto? = null
    )

    /**
     * Desconto consolidado de uma transação. Cupom acumulativo soma vários `cupons`;
     * utilização `ESTORNADO` aparece na lista mas não abate [vlDesconto].
     */
    @Serializable
    data class Desconto(
        /** Valor cheio da recarga, antes do cupom. */
        val vlBruto: Double,
        /** Soma dos descontos vigentes (estornados não entram). */
        val vlDesconto: Double,
        /** O que o cliente pagou: `vlBruto - vlDesconto`. */
        val vlPago: Double,
        val cupons: List<CupomAplicado> = emptyList()
    )

    @Serializable
    data class CupomAplicado(
        val idCupom: String,
        val vlDesconto: Double,
        /** `APLICADO` ou `ESTORNADO`. */
        val stStatus: String,
        val dtUtilizacao: String
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

/**
 * v2 do relatório: mesmo payload da v1 com `desconto` a mais em cada transação.
 * Aditiva — a v1 segue intacta.
 */
internal object RechargeReportV2Endpoint : Endpoint {
    override val path = "spec-mobile/v2/recarga/relatorio"
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

/**
 * Relatório de recargas com o desconto de cupom aplicado
 * (`GET spec-mobile/v2/recarga/relatorio`).
 *
 * Resposta idêntica à da v1 — mesmo [RechargeReportSuccess] — só que cada transação pode
 * trazer [RechargeReportSuccess.Desconto]. Quando a recarga não teve cupom, ou quando o
 * payment-gateway está fora do ar, o campo vem `null` e o relatório volta inteiro.
 */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.rechargeReportV2(request: RechargeReportRequest): RechargeReportSuccess =
    client.send(
        RechargeReportV2Endpoint,
        query = buildMap {
            put("coMsisdn", request.coMsisdn)
        }
    )
