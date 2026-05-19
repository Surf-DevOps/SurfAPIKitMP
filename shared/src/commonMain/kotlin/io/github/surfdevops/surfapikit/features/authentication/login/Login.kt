package io.github.surfdevops.surfapikit.features.authentication.login

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

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

suspend fun SurfApiKit.login(request: LoginRequest): LoginSuccess {
    val response: LoginSuccess = client.send(LoginEndpoint, body = request)
    tokenStore.selectionToken = response.resultado.selectionToken
    tokenStore.tokenType = response.resultado.tokenType
    return response
}
