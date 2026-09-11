package io.github.surfdevops.surfapikit.features.tier.consultar

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/**
 * Consulta o tier Uber Pro do motorista a partir do MSISDN da linha.
 *
 * `resultado.integrated` diz se o cliente JÁ autorizou o compartilhamento dos dados
 * com a Uber: `false` significa que o app precisa apresentar o convite (popup) antes
 * de mandar o cliente pra recarga; `true` significa que é só aplicar o layout do
 * `resultado.tier`.
 */
@Serializable
data class UberTierConsultarSuccess(
    val sucesso: Int? = null,
    val transacao: String? = null,
    val descricao: String? = null,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null,
) {
    @Serializable
    data class Resultado(
        val code: String? = null,
        val msg: String? = null,
        /** "Ouro", "Platina", "Diamante", "Azul" — nulo quando não há tier. */
        val tier: String? = null,
        val integrated: Boolean? = null,
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null,
    )
}

internal data class UberTierConsultarEndpoint(val msisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/tier/uber/$msisdn"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uberTierConsultar(msisdn: String): UberTierConsultarSuccess =
    client.send(UberTierConsultarEndpoint(msisdn))
