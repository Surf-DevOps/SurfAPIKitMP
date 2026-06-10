package io.github.surfdevops.surfapikit.features.cards.getcardsbymsisdn

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.cards.listcards.ListCardsSuccess
import io.ktor.http.HttpMethod
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

internal data class GetCardsByMsisdnEndpoint(val msisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/by-msisdn/$msisdn"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getCardsByMsisdn(msisdn: String): ListCardsSuccess =
    client.send(GetCardsByMsisdnEndpoint(msisdn))
