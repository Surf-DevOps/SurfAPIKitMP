package io.github.surfdevops.surfapikit.features.schedule.listrecurrences

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

typealias ListRecurrencesSuccess = Recurrence

internal object ListRecurrencesEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recorrencias"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.listRecurrences(coMsisdn: String): ListRecurrencesSuccess =
    client.send(ListRecurrencesEndpoint, query = mapOf("coMsisdn" to coMsisdn))
