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
    /** `tb_recarga.coRecarga` da recarga que abriu o pop-up. Precisa ser da linha do token. */
    val coRecarga: String,
    val plataforma: String,
    val versaoApp: String
)

internal object AvaliacaoExibicaoEndpoint : Endpoint {
    override val path = "spec-mobile/v1/avaliacao/exibicao"
    override val method = HttpMethod.Post
}

/**
 * Registra a exibição do pop-up como PENDENTE (HTTP 201); o `resultado.idAvaliacao` identifica a
 * avaliação nas chamadas seguintes. Idempotente por `coRecarga`, e devolve a PENDENTE que a
 * linha já tiver em aberto (`reaproveitada = true`). 404 quando a recarga não é da linha e
 * 409 quando a linha não está elegível.
 */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoExibicao(coRecarga: String): AvaliacaoSuccess =
    client.send(
        AvaliacaoExibicaoEndpoint,
        // Sem valores default no request: o Json do kit não usa encodeDefaults.
        body = AvaliacaoExibicaoRequest(
            coRecarga = coRecarga,
            plataforma = AppInfo.platform,
            versaoApp = AppInfo.appVersion
        )
    )
