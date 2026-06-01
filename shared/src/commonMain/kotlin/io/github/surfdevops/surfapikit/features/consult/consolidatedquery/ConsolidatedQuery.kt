package io.github.surfdevops.surfapikit.features.consult.consolidatedquery

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class ConsolidatedQueryRequest(val nuMsisdn: String)

@Serializable
data class ConsolidatedQuerySuccess(
    val sucesso: Int,
    val nuTransacao: String,
    val mensagem: String,
    val resultado: Resultado,
    val transaction: TransactionInfo
) {
    @Serializable
    data class Resultado(
        val coMsisdn: Long,
        val nuMsisdn: Long,
        val noMvno: String,
        val nuDocumento: String,
        @SerialName("detalhe_consumo") val detalheConsumo: DetalheConsumo? = null,
        @SerialName("detalhe_plano") val detalhePlano: DetalhePlano,
        val pixAutomatico: PixAutomatico? = null,
        val compreGanhe: CompreGanhe? = null,
        val mandouGanhou: MandouGanhou? = null
    )

    @Serializable
    data class DetalheConsumo(
        val qtVozAtribuido: Int? = null,
        val consumoVoz: Int? = null,
        val qtDadoAtribuido: Double? = null,
        val consumoDado: Double? = null,
        val qtSmsAtribuido: Int? = null,
        val consumoSms: Int? = null,
        val dtPlano: String? = null,
        val dtExpiracao: String? = null
    )

    @Serializable
    data class DetalhePlano(
        val noPlano: String,
        val nuPlano: String,
        val valor: Double,
        val diasValidade: Int,
        val qtDado: Double,
        val qtVoz: Int,
        val qtSms: Int,
        val dadoPortabilidade: Int? = null,
        val vozPortabilidade: Int? = null,
        val smsPortabilidade: Int? = null,
        val planoRecorrencia: String? = null,
        val dadoRecorrencia: Int? = null,
        val vozRecorrencia: Int? = null,
        val smsRecorrencia: Int? = null,
        val nuPlanoRecargaRapida: String? = null,
        val noPlanoRecargaRapida: String? = null,
        val valorPlanoRecargaRapida: Double? = null
    )

    @Serializable
    data class PixAutomatico(
        val available: Boolean,
        val activeAuthorization: String? = null,
        val gatewayType: String? = null
    )

    @Serializable
    data class CompreGanhe(
        val disponivel: Boolean? = null,
        val podeEscanear: Boolean? = null,
        val podeResgatar: Boolean? = null
    )

    @Serializable
    data class MandouGanhou(
        val disponivel: Boolean? = null,
        val podeResgatarLoterica: Boolean? = null,
        val podeResgatarCorreios: Boolean? = null
    )

    @Serializable
    data class TransactionInfo(
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal object ConsolidatedQueryEndpoint : Endpoint {
    override val path = "spec-mobile/v2/consumer/consulta-consolidada"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.consolidatedQuery(request: ConsolidatedQueryRequest): ConsolidatedQuerySuccess =
    client.send(ConsolidatedQueryEndpoint, query = mapOf("nuMsisdn" to request.nuMsisdn))
