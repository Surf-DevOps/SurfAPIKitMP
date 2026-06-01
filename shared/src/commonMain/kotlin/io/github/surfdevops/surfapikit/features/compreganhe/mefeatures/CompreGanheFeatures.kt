package io.github.surfdevops.surfapikit.features.compreganhe.mefeatures

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CompreGanheFeaturesSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(val compreGanhe: Features? = null)

    @Serializable
    data class Features(
        val disponivel: Boolean? = null,
        val podeEscanear: Boolean? = null,
        val podeResgatar: Boolean? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object CompreGanheFeaturesEndpoint : Endpoint {
    override val path = "spec-mobile/v1/compre-ganhe/me/features"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.compreGanheFeatures(): CompreGanheFeaturesSuccess =
    client.send(CompreGanheFeaturesEndpoint)
