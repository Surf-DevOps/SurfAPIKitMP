package io.github.surfdevops.surfapikit.features.avaliacao.responder

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoSuccess
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class AvaliacaoResponderRequest(
    /** 1 a 5. Único campo obrigatório. */
    val nota: Int,
    val categorias: List<Int>,
    /** 10 a 300 caracteres quando presente. */
    val comentario: String? = null
)

internal data class AvaliacaoResponderEndpoint(val avaliacaoId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/avaliacao/$avaliacaoId/responder"
    override val method = HttpMethod.Post
}

/** Grava nota, categorias e comentário. Status RESPONDIDA. */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoResponder(
    avaliacaoId: String,
    request: AvaliacaoResponderRequest
): AvaliacaoSuccess =
    client.send(AvaliacaoResponderEndpoint(avaliacaoId), body = request)
