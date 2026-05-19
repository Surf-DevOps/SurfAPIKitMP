package io.github.surfdevops.surfapikit.core

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

class ApiClient internal constructor(
    val environment: ApiEnvironment,
    val tokenStore: TokenStore,
    private val refreshHandler: suspend (refreshToken: String) -> BearerTokens?
) {

    internal val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
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

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenStore.accessToken
                    val refresh = tokenStore.refreshToken
                    if (!access.isNullOrEmpty()) {
                        BearerTokens(access, refresh ?: "")
                    } else null
                }
                refreshTokens {
                    val refresh = tokenStore.refreshToken ?: return@refreshTokens null
                    refreshHandler(refresh)
                }
                sendWithoutRequest { true }
            }
        }
    }

    fun clearTokens() = tokenStore.clear()

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
                query?.forEach { (k, v) -> if (v != null) parameter(k, v) }
                if (body != null) setBody(body)
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Transport(e)
        }

        val status = response.status.value
        if (status >= 400) {
            val text = runCatching { response.bodyAsText() }.getOrNull()
            throw parseApiError(text, status)
        }
        return response
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
