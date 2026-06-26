package io.github.surfdevops.surfapikit.features.dreamshaper

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class DreamShaperRequest(val nuDocumento: String)

@Serializable
data class DreamShaperSuccess(
    val cpf: String,
    @SerialName("redirect_url") val redirectUrl: String
)

internal data class DreamShaperEndpoint(val nuDocumento: String) : Endpoint {
    override val path: String = "spec-dreamshapper/v1/beneficiarios/$nuDocumento/redirect-url"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getDSUrl(request: DreamShaperRequest): DreamShaperSuccess =
    client.send(DreamShaperEndpoint(request.nuDocumento))
