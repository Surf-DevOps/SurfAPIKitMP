package io.github.surfdevops.surfapikit.features.authentication.sendcode

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class SendCodeRequest(val nuMsisdn: String)

@Serializable
data class SendCodeSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(val nuMsisdn: String)
}

internal object SendCodeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/auth/generate-code"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.sendCode(request: SendCodeRequest): SendCodeSuccess =
    client.send(SendCodeEndpoint, body = request)
