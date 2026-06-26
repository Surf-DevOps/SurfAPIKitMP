package io.github.surfdevops.surfapikit.features.authentication.login

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class LoginRequest(
    val nuDocumento: String,
    val dsPassword: String,
    val coMvno: Int
)

@Serializable
data class LoginSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val nuDocumento: String,
        val registros: List<Registro>,
        val selectionToken: String,
        val tokenType: String
    )

    @Serializable
    data class Registro(
        val coMsisdn: String,
        val dsNome: String,
        val nuMsisdn: String,
        val coMvno: Int
    )
}

internal object LoginEndpoint : Endpoint {
    override val path = "spec-mobile/v2/customer/login"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.login(request: LoginRequest): LoginSuccess {
    val response: LoginSuccess = client.send(LoginEndpoint, body = request)
    // The native iOS SDK stores the selectionToken in tokenStore.accessToken so the auth
    // header on the subsequent selectLine call uses it as Bearer. We mirror that behavior.
    tokenStore.accessToken = response.resultado.selectionToken
    tokenStore.selectionToken = response.resultado.selectionToken
    tokenStore.tokenType = response.resultado.tokenType
    return response
}
