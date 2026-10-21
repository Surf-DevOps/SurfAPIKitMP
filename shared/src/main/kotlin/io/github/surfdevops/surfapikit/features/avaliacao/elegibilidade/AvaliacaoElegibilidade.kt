package io.github.surfdevops.surfapikit.features.avaliacao.elegibilidade

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoTransaction
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class AvaliacaoElegibilidadeSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    val resultado: Resultado? = null,
    val transaction: AvaliacaoTransaction? = null
) {
    @Serializable
    data class Resultado(
        /** `false` quando a jornada está desligada na MVNO ou a linha está no cooldown. */
        val elegivel: Boolean,
        /** Ex.: `JORNADA_DESLIGADA`, `COOLDOWN`. Só informativo. */
        val motivo: String? = null,
        val cooldownDias: Int? = null,
        /** URL da loja cadastrada para a MVNO (Google Play no Android). */
        val urlLoja: String? = null
    )
}

internal object AvaliacaoElegibilidadeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/avaliacao/elegibilidade"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoElegibilidade(): AvaliacaoElegibilidadeSuccess =
    client.send(AvaliacaoElegibilidadeEndpoint)
