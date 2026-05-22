package io.github.surfdevops.surfapikit.features.schedule.dismisspopuprecurrence

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

internal data class DismissPopupRecurrenceEndpoint(val recurrenceId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/recorrencias/$recurrenceId/dispensar-popup"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.dismissPopupRecurrence(recurrenceId: String) {
    client.sendVoid(DismissPopupRecurrenceEndpoint(recurrenceId))
}
