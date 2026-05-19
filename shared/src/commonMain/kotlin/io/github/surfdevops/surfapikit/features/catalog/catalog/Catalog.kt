package io.github.surfdevops.surfapikit.features.catalog.catalog

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class CatalogRequest(
    val coMsisdn: String,
    val responsive: Boolean
)

@Serializable
data class CatalogSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    val resultado: List<CustomerResult>,
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
        val stPlanoControle: Int? = null,
        val ratingGroups: List<RatingGroup>? = null,
        val noPacote: String? = null,
        val totalDados: String? = null,
        val internetSemCortes: String,
        val detalhamentoInternet: List<DetalhamentoInternet>? = null,
        val pacote: Pacote? = null,
        val sva: SVA,
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
        val plano: String? = null,
        val imagem: String? = null
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

internal object CatalogEndpoint : Endpoint {
    override val path = "spec-mobile/v1/portfolio/catalogo"
    override val method = HttpMethod.Get
}

suspend fun SurfApiKit.getCatalog(request: CatalogRequest): CatalogSuccess =
    client.send(
        CatalogEndpoint,
        query = mapOf("coMsisdn" to request.coMsisdn, "responsivo" to request.responsive.toString())
    )
