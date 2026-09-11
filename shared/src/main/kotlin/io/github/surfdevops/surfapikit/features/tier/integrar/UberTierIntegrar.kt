package io.github.surfdevops.surfapikit.features.tier.integrar

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class UberTierIntegrarRequest(val msisdn: String)

/**
 * Abre a solicitação de integração com a Uber e devolve a URL de autorização (OAuth)
 * que o app carrega numa WebView.
 *
 * `resultado.solicitacaoId` é o identificador que volta como `solicitacaoId` na
 * concretização. O gateway também o ecoa como `state` no OAuth, então o campo
 * [UberTierIntegrarSuccess.Resultado.state] existe como fallback caso a resposta traga
 * só ele.
 */
@Serializable
data class UberTierIntegrarSuccess(
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
        val authorizeUrl: String? = null,
        val solicitacaoId: String? = null,
        val state: String? = null,
        val redirectUri: String? = null,
    ) {
        /** `solicitacaoId` com fallback para `state` — é o que o /concretizar espera. */
        val requestId: String? get() = solicitacaoId ?: state
    }

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null,
    )
}

internal object UberTierIntegrarEndpoint : Endpoint {
    override val path = "spec-mobile/v1/tier/integracoes/uber"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uberTierIntegrar(request: UberTierIntegrarRequest): UberTierIntegrarSuccess =
    client.send(UberTierIntegrarEndpoint, body = request)
