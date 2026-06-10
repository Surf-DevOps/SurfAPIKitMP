package io.github.surfdevops.surfapikit.features.compreganhe.planos

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanhePlanosSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val saldoAtual: Int? = null,
        val planos: List<PlanoBonus> = emptyList()
    )

    @Serializable
    data class PlanoBonus(
        val id: String,
        val codigoPlano: String? = null,
        val nome: String? = null,
        val tituloCurto: String? = null,
        val descricao: String? = null,
        val valorPontos: Int? = null,
        val addonTipo: String? = null,
        val addonQuantidade: Int? = null,
        val validadeDias: Int? = null,
        val ordem: Int? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanhePlanosEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/planos"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanhePlanos(): CompreGanhePlanosSuccess =
    client.send(CompreGanhePlanosEndpoint)
