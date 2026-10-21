package io.github.surfdevops.surfapikit.features.avaliacao.loja

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoSuccess
import kotlin.coroutines.cancellation.CancellationException

internal data class AvaliacaoLojaEndpoint(val avaliacaoId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/avaliacao/$avaliacaoId/loja"
    override val method = HttpMethod.Patch
}

/** Registra o toque em "Avaliar na loja" (notas 4 e 5). O app abre a loja em seguida. */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoLoja(avaliacaoId: String): AvaliacaoSuccess =
    client.send(AvaliacaoLojaEndpoint(avaliacaoId))
