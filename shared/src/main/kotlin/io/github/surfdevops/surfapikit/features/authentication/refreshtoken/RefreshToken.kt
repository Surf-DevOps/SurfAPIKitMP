package io.github.surfdevops.surfapikit.features.authentication.refreshtoken

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.await
import io.github.surfdevops.surfapikit.core.joinUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.cancellation.CancellationException

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

private val refreshJson = Json { ignoreUnknownKeys = true; isLenient = true }
private val jsonMedia = "application/json; charset=utf-8".toMediaType()

internal suspend fun SurfApiKit.refreshTokenInternal(request: RefreshTokenRequest): RefreshTokenSuccess {
    val httpRequest = Request.Builder()
        .url(joinUrl(client.environment.baseUrl, RefreshTokenEndpoint.path))
        .post(refreshJson.encodeToString(RefreshTokenRequest.serializer(), request).toRequestBody(jsonMedia))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .build()

    // Bare client (no auth interceptor): the refresh call must not itself be intercepted.
    val response = client.rawClient.newCall(httpRequest).await()
    return response.use { resp ->
        val text = resp.body?.string()
        if (!resp.isSuccessful) {
            throw ApiError.Server(status = resp.code, serverMessage = text)
        }
        val result = refreshJson.decodeFromString(
            RefreshTokenSuccess.serializer(),
            text ?: throw ApiError.Unknown,
        )
        tokenStore.accessToken = result.resultado.accessToken
        tokenStore.refreshToken = result.resultado.refreshToken
        if (tokenStore.tokenType.isNullOrEmpty()) tokenStore.tokenType = "Bearer"
        result
    }
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.refreshToken(request: RefreshTokenRequest): RefreshTokenSuccess =
    refreshTokenInternal(request)
