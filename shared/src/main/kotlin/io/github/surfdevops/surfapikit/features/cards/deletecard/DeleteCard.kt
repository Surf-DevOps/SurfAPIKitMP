package io.github.surfdevops.surfapikit.features.cards.deletecard

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

internal data class DeleteCardEndpoint(val coMsisdn: String, val cardId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/cards/customers/$coMsisdn/cards/$cardId"
    override val method = HttpMethod.Delete
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.deleteCard(coMsisdn: String, cardId: String) {
    client.sendVoid(DeleteCardEndpoint(coMsisdn, cardId))
}
