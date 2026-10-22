package io.github.surfdevops.surfapikit.features.avaliacao.dispensar

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoSuccess
import kotlin.coroutines.cancellation.CancellationException

internal data class AvaliacaoDispensarEndpoint(val avaliacaoId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/avaliacao/$avaliacaoId/dispensar"
    override val method = HttpMethod.Patch
}

/** Toque fora ou swipe antes de responder: DISPENSADA ("não respondeu"). Idempotente; RESPONDIDA dá 409. */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoDispensar(avaliacaoId: String): AvaliacaoSuccess =
    client.send(AvaliacaoDispensarEndpoint(avaliacaoId))
