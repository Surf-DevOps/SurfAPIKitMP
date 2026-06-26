package io.github.surfdevops.surfapikit.features.authentication.resendcode

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ResendCodeRequest(val nuMsisdn: String)

@Serializable
data class ResendCodeSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: ResultadoToken? = null
) {
    @Serializable
    data class ResultadoToken(val nuMsisdn: String)
}

internal object ResendCodeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/auth/resend-code"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.resendCode(request: ResendCodeRequest): ResendCodeSuccess =
    client.send(ResendCodeEndpoint, body = request)
