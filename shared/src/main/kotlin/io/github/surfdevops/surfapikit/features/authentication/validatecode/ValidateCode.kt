package io.github.surfdevops.surfapikit.features.authentication.validatecode

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ValidateCodeRequest(
    val nuMsisdn: String,
    val nuPin: String
)

@Serializable
data class ValidateCodeSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: ResultadoToken? = null
) {
    @Serializable
    data class ResultadoToken(
        val nuMsisdn: String,
        val accessToken: String,
        val dtExpiracao: String
    )
}

internal object ValidateCodeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/auth/validate-code"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.validateCode(request: ValidateCodeRequest): ValidateCodeSuccess {
    val response: ValidateCodeSuccess = client.send(ValidateCodeEndpoint, body = request)
    response.resultado?.let { res ->
        tokenStore.accessToken = res.accessToken
        tokenStore.tokenType = "Bearer"
    }
    return response
}
