package io.github.surfdevops.surfapikit.features.avaliacao.categorias

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.avaliacao.AvaliacaoTransaction
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class AvaliacaoCategoriasSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    /** Catálogo único (as 8 categorias macro), igual para todas as notas. */
    val resultado: List<Categoria> = emptyList(),
    val transaction: AvaliacaoTransaction? = null
) {
    /** Formato confirmado no spec-mobile em 21/10/2026. */
    @Serializable
    data class Categoria(
        val id: Int,
        val codigo: String,
        val titulo: String,
        /** Subitens da categoria em texto corrido. */
        val descricao: String? = null
    )
}

internal object AvaliacaoCategoriasEndpoint : Endpoint {
    override val path = "spec-mobile/v1/avaliacao/categorias"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoCategorias(): AvaliacaoCategoriasSuccess =
    client.send(AvaliacaoCategoriasEndpoint)
