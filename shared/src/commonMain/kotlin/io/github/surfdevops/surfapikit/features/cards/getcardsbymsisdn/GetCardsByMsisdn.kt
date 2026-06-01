package io.github.surfdevops.surfapikit.features.cards.getcardsbymsisdn

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.cards.listcards.Card
import io.github.surfdevops.surfapikit.features.cards.listcards.ListCardsTransaction
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class GetCardsByMsisdnSuccess(
    val status: Int,
    val message: String,
    val content: List<Card>? = null,
    val transaction: ListCardsTransaction? = null
)

internal data class GetCardsByMsisdnEndpoint(val msisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/by-msisdn/$msisdn"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getCardsByMsisdn(msisdn: String): GetCardsByMsisdnSuccess =
    client.send(GetCardsByMsisdnEndpoint(msisdn))
