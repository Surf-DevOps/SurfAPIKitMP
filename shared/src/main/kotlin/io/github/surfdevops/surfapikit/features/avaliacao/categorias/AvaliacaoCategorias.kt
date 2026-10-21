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
    val resultado: Resultado? = null,
    val transaction: AvaliacaoTransaction? = null
) {
    @Serializable
    data class Resultado(val categorias: List<Categoria> = emptyList())

    @Serializable
    data class Categoria(
        val id: Int,
        val nome: String,
        /** Grupo de notas em que a categoria aparece: [GRUPO_NEGATIVO] (1 a 3) ou [GRUPO_POSITIVO] (4 e 5). */
        val grupo: String,
        val ordem: Int? = null
    )

    companion object {
        const val GRUPO_NEGATIVO = "NEGATIVO"
        const val GRUPO_POSITIVO = "POSITIVO"
    }
}

internal object AvaliacaoCategoriasEndpoint : Endpoint {
    override val path = "spec-mobile/v1/avaliacao/categorias"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.avaliacaoCategorias(): AvaliacaoCategoriasSuccess =
    client.send(AvaliacaoCategoriasEndpoint)
