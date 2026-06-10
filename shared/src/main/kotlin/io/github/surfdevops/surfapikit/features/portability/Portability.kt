package io.github.surfdevops.surfapikit.features.portability

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class PortabilityRequest(
    val msisdn: Long,
    val msisdnOutraOperadora: Long,
    val operadora: String,
    val documento: String,
    val nomeCompleto: String
)

@Serializable
data class PortabilityResponse(
    val coPortabilidade: String,
    val coMsisdn: Long,
    val nuMsisdnOrigem: Long,
    val nuMsisdnOutraOperadora: Long,
    val ticket: String,
    val dtCadastro: String
)

@Serializable
data class PortabilityStatusResponse(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null
) {
    @Serializable
    data class Resultado(
        val coPortabilidade: String,
        val coMsisdn: Long,
        val nuMsisdnOrigem: Long,
        val nuMsisdnOutraOperadora: Long,
        val stFinalizado: Int,
        val dtCadastro: String,
        val dtAlteracao: String,
        val coPortabilidadeTicket: Long,
        val dsTicketStatus: String,
        val descricaoTicketStatus: String,
        val dtJanelaPortabilidade: String,
        val noCliente: String,
        val nuDocumento: String,
        val nuReceptora: String,
        val nuDoadora: String,
        val dtRegistro: String,
        val dtConfirmacao: String? = null,
        val dtAutorizacao: String? = null,
        val dtFinalizacao: String? = null,
        val recusa: Recusa? = null
    )

    @Serializable
    data class Recusa(val motivo: String, val mensagem: String, val codigo: String, val causa: String)
}

@Serializable
data class FinalizePortabilityResponse(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: FinalizePortabilityResultado? = null
) {
    @Serializable
    data class FinalizePortabilityResultado(
        val coPortabilidade: String,
        val coMsisdn: Long,
        val nuMsisdnOrigem: Long,
        val nuMsisdnOutraOperadora: Long,
        val stFinalizado: Int,
        val dtCadastro: String,
        val dtAlteracao: String
    )
}

internal object RequestPortabilityEndpoint : Endpoint {
    override val path = "spec-mobile/v1/portabilidade/solicitar"
    override val method = HttpMethod.Post
}

internal data class StatusPortabilityEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/portabilidade/status/$coMsisdn"
    override val method = HttpMethod.Get
}

internal data class FinalizePortabilityEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/portabilidade/finalizar/$coMsisdn"
    override val method = HttpMethod.Patch
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.requestPortability(request: PortabilityRequest): PortabilityResponse =
    client.send(RequestPortabilityEndpoint, body = request)

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.statusPortability(coMsisdn: String): PortabilityStatusResponse =
    client.send(StatusPortabilityEndpoint(coMsisdn))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.finalizePortability(coMsisdn: String): FinalizePortabilityResponse =
    client.send(FinalizePortabilityEndpoint(coMsisdn))
