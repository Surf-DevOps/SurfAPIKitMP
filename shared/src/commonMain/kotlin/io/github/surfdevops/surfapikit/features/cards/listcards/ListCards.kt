package io.github.surfdevops.surfapikit.features.cards.listcards

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ListCardsSuccess(
    val status: Int,
    val message: String,
    val content: List<Card>? = null
)

@Serializable
data class Card(
    val cardId: String,
    val token: String,
    val customerId: String,
    val isDefault: Boolean,
    val gatewayCardId: String,
    val bin: String? = null,
    val lastFour: String? = null,
    val expiration: String? = null,
    val flag: String? = null,
    val paymentType: String? = null,
    val createdAt: String,
    val updatedAt: String
)

internal data class ListCardsEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/customers/$coMsisdn/cards"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.listCards(coMsisdn: String): ListCardsSuccess =
    client.send(ListCardsEndpoint(coMsisdn))
