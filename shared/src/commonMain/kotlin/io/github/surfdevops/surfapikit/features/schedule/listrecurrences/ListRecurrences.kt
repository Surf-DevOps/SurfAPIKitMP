package io.github.surfdevops.surfapikit.features.schedule.listrecurrences

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.features.schedule.Recurrence
import io.ktor.http.HttpMethod

typealias ListRecurrencesSuccess = Recurrence

internal object ListRecurrencesEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recorrencias"
    override val method = HttpMethod.Get
}

suspend fun SurfApiKit.listRecurrences(coMsisdn: String): ListRecurrencesSuccess =
    client.send(ListRecurrencesEndpoint, query = mapOf("coMsisdn" to coMsisdn))
