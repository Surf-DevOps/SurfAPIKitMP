package io.github.surfdevops.surfapikit.features.cards.updatecard

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCardRequest(val isDefault: Boolean)

@Serializable
data class UpdateCardSuccess(
    val status: Int,
    val message: String,
    val content: CardContent
) {
    @Serializable
    data class CardContent(
        val cardId: String,
        val token: String,
        val customerId: String,
        val isDefault: Boolean,
        val gatewayCardId: String,
        val createdAt: String,
        val updatedAt: String
    )
}

internal data class UpdateCardEndpoint(val coMsisdn: String, val cardId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/customers/$coMsisdn/cards/$cardId"
    override val method = HttpMethod.Patch
}

suspend fun SurfApiKit.updateCard(
    request: UpdateCardRequest,
    coMsisdn: String,
    cardId: String
): UpdateCardSuccess =
    client.send(UpdateCardEndpoint(coMsisdn, cardId), body = request)
