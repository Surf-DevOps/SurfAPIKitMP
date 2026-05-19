package io.github.surfdevops.surfapikit.features.authentication.refreshtoken

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.joinUrl
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class RefreshTokenSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val accessToken: String,
        val refreshToken: String,
        val dtExpiracao: String,
        val dtExpiracaoRefresh: String
    )
}

internal object RefreshTokenEndpoint : Endpoint {
    override val path = "spec-mobile/v2/auth/refresh"
    override val method = HttpMethod.Post
}

internal suspend fun SurfApiKit.refreshTokenInternal(request: RefreshTokenRequest): RefreshTokenSuccess {
    val fullUrl = joinUrl(client.environment.baseUrl, RefreshTokenEndpoint.path)
    val response: HttpResponse = client.http.request {
        method = HttpMethod.Post
        url(fullUrl)
        headers {
            append("Content-Type", "application/json")
            append("Accept", "application/json")
        }
        setBody(request)
    }
    if (response.status.value >= 400) {
        throw io.github.surfdevops.surfapikit.core.ApiError.Server(
            status = response.status.value,
            serverMessage = runCatching { response.bodyAsText() }.getOrNull()
        )
    }
    val result: RefreshTokenSuccess = response.body()
    tokenStore.accessToken = result.resultado.accessToken
    tokenStore.refreshToken = result.resultado.refreshToken
    if (tokenStore.tokenType.isNullOrEmpty()) tokenStore.tokenType = "Bearer"
    return result
}

suspend fun SurfApiKit.refreshToken(request: RefreshTokenRequest): RefreshTokenSuccess =
    refreshTokenInternal(request)
