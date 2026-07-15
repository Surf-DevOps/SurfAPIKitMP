package io.github.surfdevops.surfapikit.features.compreganhe.adminplanos

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

/**
 * Planos de bônus vindos do painel admin (`compre-ganhe-admin/planos/{mvnoId}`).
 * Diferente do endpoint mobile, retorna `itens` (com `ativo`/`valorReais`) e
 * paginação, autenticado via header `X-Admin-Token`.
 */
@Serializable
data class CompreGanheAdminPlanosSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val itens: List<Item> = emptyList(),
        val paginacao: Paginacao? = null
    )

    @Serializable
    data class Item(
        val id: String,
        val mvnoId: String? = null,
        val codigoPlano: String? = null,
        val nome: String? = null,
        val tituloCurto: String? = null,
        val descricao: String? = null,
        val valorPontos: Int? = null,
        val valorReais: Double? = null,
        val addonTipo: String? = null,
        val addonQuantidade: Int? = null,
        val validadeDias: Int? = null,
        val ordem: Int? = null,
        val ativo: Boolean? = null,
        val criadoEm: String? = null
    )

    @Serializable
    data class Paginacao(
        val totalItens: Int? = null,
        val totalPaginas: Int? = null,
        val paginaAtual: Int? = null,
        val limite: Int? = null,
        val temProxima: Boolean? = null,
        val temAnterior: Boolean? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal class CompreGanheAdminPlanosEndpoint(
    mvnoId: Int,
    private val adminToken: String
) : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe-admin/planos/$mvnoId"
    override val method = HttpMethod.Get
    override val headers: Map<String, String>
        get() = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "X-Admin-Token" to adminToken
        )
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheAdminPlanos(
    mvnoId: Int,
    adminToken: String
): CompreGanheAdminPlanosSuccess =
    client.send(CompreGanheAdminPlanosEndpoint(mvnoId, adminToken))
