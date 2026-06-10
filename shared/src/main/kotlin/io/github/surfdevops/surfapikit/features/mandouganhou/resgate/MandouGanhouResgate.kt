package io.github.surfdevops.surfapikit.features.mandouganhou.resgate

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.mandouganhou.MandouGanhouTipo
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class MandouGanhouResgateRequest(
    val tipo: MandouGanhouTipo,
    val codigo: String,
    val msisdnDestino: String? = null
)

@Serializable
data class MandouGanhouResgateSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val resgateId: String,
        val status: String? = null,
        val tipo: String? = null,
        val codigo: String? = null,
        val subtipo: String? = null,
        val bonusMb: Int? = null,
        val nuPlano: String? = null,
        val noPlano: String? = null,
        val protocoloProvisionamento: String? = null,
        val msisdnDestino: String? = null,
        val dataResgate: String? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object MandouGanhouResgateEndpoint : Endpoint {
    override val path = "spec-mobile/v1/mandou-ganhou/resgate"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.mandouGanhouResgatar(request: MandouGanhouResgateRequest): MandouGanhouResgateSuccess =
    client.send(MandouGanhouResgateEndpoint, body = request)
