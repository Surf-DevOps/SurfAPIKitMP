package io.github.surfdevops.surfapikit.features.cards.createcard

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class CreateCardRequest(
    val holderName: String,
    val cardNumber: String,
    val holderDocument: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val paymentType: String? = null,
    val isDefault: Boolean? = null
)

@Serializable
data class CreateCardSuccess(
    val status: Int,
    val message: String,
    val content: CardContent? = null
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

internal data class CreateCardEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/customers/$coMsisdn/cards"
    override val method = HttpMethod.Post
}

suspend fun SurfApiKit.createCard(request: CreateCardRequest, coMsisdn: String): CreateCardSuccess =
    client.send(CreateCardEndpoint(coMsisdn), body = request)
