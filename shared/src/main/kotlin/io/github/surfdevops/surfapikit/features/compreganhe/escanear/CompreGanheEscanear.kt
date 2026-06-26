package io.github.surfdevops.surfapikit.features.compreganhe.escanear

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanheEscanearRequest(val qrUrl: String)

@Serializable
data class CompreGanheEscanearSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val pontosAcumulados: Int? = null,
        val saldoTotal: Int? = null,
        val validade: String? = null,
        val cnpjEmissor: String? = null,
        val valorCupom: Double? = null,
        val dataCompra: String? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanheEscanearEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/escanear"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheEscanear(request: CompreGanheEscanearRequest): CompreGanheEscanearSuccess =
    client.send(CompreGanheEscanearEndpoint, body = request)
