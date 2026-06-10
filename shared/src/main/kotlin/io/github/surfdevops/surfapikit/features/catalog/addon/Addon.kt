package io.github.surfdevops.surfapikit.features.catalog.addon

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class AddonRequest(val coMsisdn: String)

@Serializable
data class AddonSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    val bloqueado: Boolean? = null,
    val resultado: List<CustomerResult>? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class CustomerResult(
        val coPlano: Int,
        val nuPlano: String,
        val noPlano: String,
        val qtDiaValidade: Int,
        val vlPlano: Int,
        val qtDado: Int,
        val qtSMS: String,
        val qtVoz: String,
        val stVisivelNoApp: Int,
        val ratingGroups: List<RatingGroup>? = null,
        val noPacote: String? = null,
        val totalDados: String,
        val internetSemCortes: String? = null,
        val detalhamentoInternet: List<DetalhamentoInternet>? = null,
        val pacote: Pacote? = null,
        val sva: SVA? = null,
        val parcelas: Int
    )

    @Serializable
    data class DetalhamentoInternet(
        val noDados: String,
        val qtDados: String? = null
    )

    @Serializable
    data class Pacote(
        val id: Int? = null,
        val nome: String? = null,
        val imagem: String? = null
    )

    @Serializable
    data class SVA(
        val existeSVA: Boolean,
        val plano: String? = null
    )

    @Serializable
    data class RatingGroup(
        val imagem: String? = null,
        val coRatingGroup: Int,
        val noRatingGroup: String
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal data class AddonEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/portfolio/catalogo/addon"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getAddon(request: AddonRequest): AddonSuccess =
    client.send(AddonEndpoint(request.coMsisdn), query = mapOf("coMsisdn" to request.coMsisdn))
