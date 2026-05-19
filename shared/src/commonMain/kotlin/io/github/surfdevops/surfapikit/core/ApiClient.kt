package io.github.surfdevops.surfapikit.core

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.parameters
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

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

    internal val http: HttpClient = HttpClient {
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

        defaultRequest {
            url(environment.baseUrl)
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
            if (T::class == HttpResponse::class) {
                @Suppress("UNCHECKED_CAST")
                response as T
            } else {
                response.body()
            }
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
                url {
                    takeFrom(environment.baseUrl)
                    appendPath(endpoint.path)
                    query?.forEach { (k, v) -> if (v != null) parameters.append(k, v.toString()) }
                }
                headers {
                    endpoint.headers.forEach { (k, v) -> append(k, v) }
                }
                if (body != null) {
                    contentTypeJson()
                    setBody(body)
                }
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

    @PublishedApi
    internal fun HttpRequestBuilder.contentTypeJson() {
        headers.remove("Content-Type")
        headers.append("Content-Type", "application/json")
    }

    private fun URLBuilder.appendPath(path: String) {
        val clean = path.removePrefix("/")
        encodedPath = (encodedPath.trimEnd('/') + "/" + clean)
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
                val descricao = obj["descricao"]?.jsonPrimitiveOrNull()?.contentOrNull
                if (descricao != null) {
                    val codigo = obj["erro"]?.jsonPrimitiveOrNull()?.intOrNull ?: statusCode
                    return ApiError.Api(codigo, descricao)
                }
                obj["message"]?.jsonPrimitiveOrNull()?.contentOrNull?.let {
                    return ApiError.Api(statusCode, it)
                }
                obj["error"]?.jsonPrimitiveOrNull()?.contentOrNull?.let {
                    return ApiError.Api(statusCode, it)
                }
            }
        return ApiError.Server(statusCode, null, "Erro ao processar requisição")
    }

    private fun JsonElement?.jsonPrimitiveOrNull(): JsonPrimitive? = (this as? JsonPrimitive)
}
