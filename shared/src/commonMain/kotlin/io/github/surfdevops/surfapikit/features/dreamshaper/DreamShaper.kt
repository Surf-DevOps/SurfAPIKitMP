package io.github.surfdevops.surfapikit.features.dreamshaper

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

suspend fun SurfApiKit.getDSUrl(request: DreamShaperRequest): DreamShaperSuccess =
    client.send(DreamShaperEndpoint(request.nuDocumento))
