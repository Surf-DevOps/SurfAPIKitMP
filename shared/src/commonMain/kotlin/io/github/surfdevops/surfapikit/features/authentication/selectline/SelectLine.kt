package io.github.surfdevops.surfapikit.features.authentication.selectline

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class SelectLineRequest(val nuMsisdn: String)

@Serializable
data class SelectLineSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val nuMsisdn: String,
        val nuDocumento: String,
        val dsNome: String,
        val accessToken: String,
        val refreshToken: String,
        val dtExpiracao: String,
        val dtExpiracaoRefresh: String,
        val tokenType: String
    )
}

internal object SelectLineEndpoint : Endpoint {
    override val path = "spec-mobile/v2/auth/select-line"
    override val method = HttpMethod.Post
}

suspend fun SurfApiKit.selectLine(request: SelectLineRequest): SelectLineSuccess {
    val response: SelectLineSuccess = client.send(SelectLineEndpoint, body = request)
    tokenStore.accessToken = response.resultado.accessToken
    tokenStore.tokenType = response.resultado.tokenType
    tokenStore.refreshToken = response.resultado.refreshToken
    return response
}
