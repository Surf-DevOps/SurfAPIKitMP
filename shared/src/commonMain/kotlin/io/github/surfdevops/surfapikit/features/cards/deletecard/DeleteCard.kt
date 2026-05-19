package io.github.surfdevops.surfapikit.features.cards.deletecard

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod

internal data class DeleteCardEndpoint(val coMsisdn: String, val cardId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/customers/$coMsisdn/cards/$cardId"
    override val method = HttpMethod.Delete
}

suspend fun SurfApiKit.deleteCard(coMsisdn: String, cardId: String) {
    client.sendVoid(DeleteCardEndpoint(coMsisdn, cardId))
}
