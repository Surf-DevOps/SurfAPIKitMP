package io.github.surfdevops.surfapikit.core

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

internal const val REFRESH_PATH = "spec-mobile/v2/auth/refresh"

class ApiClient internal constructor(
    val environment: ApiEnvironment,
    val tokenStore: TokenStore
) {

    internal val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    // Bare HTTP client used exclusively for the refresh-token endpoint, so the refresh
    // request itself is not affected by the auth-injecting defaultRequest below.
    internal val refreshHttp: HttpClient = HttpClient {
        expectSuccess = false
        install(HttpTimeout) {
            requestTimeoutMillis = 905_000
            connectTimeoutMillis = 905_000
            socketTimeoutMillis = 905_000
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    val http: HttpClient = HttpClient {
        expectSuccess = false

        install(HttpTimeout) {
            requestTimeoutMillis = 905_000
            connectTimeoutMillis = 905_000
            socketTimeoutMillis = 905_000
        }

        install(ContentNegotiation) {
            json(json)
        }
    }

    // Mirrors the native iOS SDK's AuthInterceptor.adapt — runs per request, so updates
    // to tokenStore (login → selectLine → refresh) propagate immediately.
    private fun HttpRequestBuilder.applyAuth() {
        val token = tokenStore.accessToken
        if (!token.isNullOrEmpty()) {
            val type = tokenStore.tokenType?.takeIf { it.isNotEmpty() } ?: "Bearer"
            headers.remove("Authorization")
            headers.append("Authorization", "$type $token")
        }
    }

    fun clearTokens() = tokenStore.clear()

    internal suspend fun refreshTokensNow(refreshToken: String): BearerTokensLite? {
        val response: HttpResponse = runCatching {
            refreshHttp.request {
                method = HttpMethod.Post
                url(joinUrl(environment.baseUrl, REFRESH_PATH))
                headers {
                    append("Content-Type", "application/json")
                    append("Accept", "application/json")
                }
                setBody(RefreshRequestDto(refreshToken))
            }
        }.getOrElse { return null }

        if (response.status.value >= 400) {
            tokenStore.clear()
            return null
        }
        return runCatching {
            val parsed: RefreshResponseDto = response.body()
            tokenStore.accessToken = parsed.resultado.accessToken
            tokenStore.refreshToken = parsed.resultado.refreshToken
            if (tokenStore.tokenType.isNullOrEmpty()) tokenStore.tokenType = "Bearer"
            BearerTokensLite(parsed.resultado.accessToken, parsed.resultado.refreshToken)
        }.getOrNull()
    }

    suspend inline fun <reified T> send(
        endpoint: Endpoint,
        body: Any? = null,
        query: Map<String, Any?>? = null
    ): T {
        val response = execute(endpoint, body, query, allowEmpty = false)
        return try {
            response.body()
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Decoding(e)
        }
    }

    suspend fun sendVoid(
        endpoint: Endpoint,
        body: Any? = null,
        query: Map<String, Any?>? = null
    ) {
        execute(endpoint, body, query, allowEmpty = true)
    }

    @PublishedApi
    internal suspend fun execute(
        endpoint: Endpoint,
        body: Any?,
        query: Map<String, Any?>?,
        allowEmpty: Boolean
    ): HttpResponse {
        val response: HttpResponse = try {
            http.request {
                method = endpoint.method
                url(joinUrl(environment.baseUrl, endpoint.path))
                headers {
                    endpoint.headers.forEach { (k, v) -> append(k, v) }
                }
                applyAuth()
                query?.forEach { (k, v) -> if (v != null) parameter(k, v) }
                if (body != null) setBody(body)
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Transport(e)
        }

        val status = response.status.value
        if (status < 400) return response

        val text = runCatching { response.bodyAsText() }.getOrNull()
        val err = parseApiError(text, status)

        // Auto-refresh + retry once when the server signals an invalid/missing access token.
        // Triggered on HTTP 401 OR on the API-specific `erro=6` ("Token é um parametro obrigatório").
        val isAuthError = status == 401 || (err is ApiError.Api && err.code == 6)
        val canRefreshRetry = isAuthError && !tokenStore.refreshToken.isNullOrEmpty()
        if (!canRefreshRetry) throw err

        refreshTokensNow(tokenStore.refreshToken!!) ?: throw err

        val retry: HttpResponse = try {
            http.request {
                method = endpoint.method
                url(joinUrl(environment.baseUrl, endpoint.path))
                headers {
                    endpoint.headers.forEach { (k, v) -> append(k, v) }
                }
                applyAuth()
                query?.forEach { (k, v) -> if (v != null) parameter(k, v) }
                if (body != null) setBody(body)
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Transport(e)
        }
        if (retry.status.value < 400) return retry
        val retryText = runCatching { retry.bodyAsText() }.getOrNull()
        throw parseApiError(retryText, retry.status.value)
    }

    private fun parseApiError(text: String?, statusCode: Int): ApiError {
        if (text.isNullOrEmpty()) {
            return ApiError.Server(statusCode, null, "Sem resposta do servidor")
        }
        runCatching { json.decodeFromString<ApiErrorResponse>(text) }
            .getOrNull()
            ?.let { return ApiError.Api(it.erro, it.descricao) }

        runCatching { json.parseToJsonElement(text) as? JsonObject }
            .getOrNull()?.let { obj ->
                val descricao = (obj["descricao"] as? JsonPrimitive)?.contentOrNull
                if (descricao != null) {
                    val codigo = (obj["erro"] as? JsonPrimitive)?.intOrNull ?: statusCode
                    return ApiError.Api(codigo, descricao)
                }
                (obj["message"] as? JsonPrimitive)?.contentOrNull?.let {
                    return ApiError.Api(statusCode, it)
                }
                (obj["error"] as? JsonPrimitive)?.contentOrNull?.let {
                    return ApiError.Api(statusCode, it)
                }
            }
        return ApiError.Server(statusCode, null, "Erro ao processar requisição")
    }
}

internal fun joinUrl(baseUrl: String, path: String): String {
    val cleanBase = baseUrl.trimEnd('/')
    val cleanPath = path.removePrefix("/")
    return "$cleanBase/$cleanPath"
}

internal data class BearerTokensLite(val accessToken: String, val refreshToken: String)

@Serializable
private data class RefreshRequestDto(val refreshToken: String)

@Serializable
private data class RefreshResponseDto(val resultado: Resultado) {
    @Serializable
    data class Resultado(val accessToken: String, val refreshToken: String)
}
