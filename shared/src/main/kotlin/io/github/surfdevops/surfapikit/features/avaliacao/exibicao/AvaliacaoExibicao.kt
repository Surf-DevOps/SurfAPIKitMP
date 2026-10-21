package io.github.surfdevops.surfapikit.features.avaliacao.exibicao

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoSuccess
import io.github.surfdevops.surfapikit.platform.AppInfo
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class AvaliacaoExibicaoRequest(
    val origem: String,
    val plataforma: String,
    val versaoApp: String
) {
    companion object {
        const val ORIGEM_RECARGA_REALIZADA = "RECARGA_REALIZADA"
    }
}

internal object AvaliacaoExibicaoEndpoint : Endpoint {
    override val path = "spec-mobile/v1/avaliacao/exibicao"
    override val method = HttpMethod.Post
}

/** Registra a exibição do pop-up como PENDENTE; o `resultado.id` identifica a avaliação nas chamadas seguintes. */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoExibicao(
    origem: String = AvaliacaoExibicaoRequest.ORIGEM_RECARGA_REALIZADA
): AvaliacaoSuccess =
    client.send(
        AvaliacaoExibicaoEndpoint,
        // Sem valores default no request: o Json do kit não usa encodeDefaults.
        body = AvaliacaoExibicaoRequest(
            origem = origem,
            plataforma = AppInfo.platform,
            versaoApp = AppInfo.appVersion
        )
    )
