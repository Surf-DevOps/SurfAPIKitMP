package io.github.surfdevops.surfapikit.core

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response as OkHttpResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal const val REFRESH_PATH = "spec-mobile/v2/auth/refresh"

private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

/**
 * Generic dynamic Retrofit service. The SDK keeps its `send(endpoint, body, query)`
 * facade, so instead of one typed interface method per endpoint we expose one method per
 * HTTP verb with a fully-resolved `@Url` (base + path + query already baked in) and a
 * pre-serialized JSON `@Body`. The response is read as a raw [ResponseBody] and decoded by
 * the caller via kotlinx.serialization.
 */
internal interface ApiService {
    @GET
    suspend fun get(@Url url: String, @HeaderMap headers: Map<String, String>): Response<ResponseBody>

    // Bodyless overloads (no @Body) are used when the caller passes no body, so the request
    // goes out without forcing an empty entity — matching the original Ktor behavior and
    // avoiding DELETE-with-body / empty-JSON-body quirks on strict gateways.
    @POST
    suspend fun post(@Url url: String, @HeaderMap headers: Map<String, String>): Response<ResponseBody>

    @POST
    suspend fun post(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @PUT
    suspend fun put(@Url url: String, @HeaderMap headers: Map<String, String>): Response<ResponseBody>

    @PUT
    suspend fun put(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @PATCH
    suspend fun patch(@Url url: String, @HeaderMap headers: Map<String, String>): Response<ResponseBody>

    @PATCH
    suspend fun patch(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody,
    ): Response<ResponseBody>

    @HTTP(method = "DELETE", hasBody = false)
    suspend fun delete(@Url url: String, @HeaderMap headers: Map<String, String>): Response<ResponseBody>

    @HTTP(method = "DELETE", hasBody = true)
    suspend fun deleteWithBody(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @Body body: RequestBody,
    ): Response<ResponseBody>
}

/**
 * Injects `Authorization: <type> <accessToken>` on every request from the token store.
 * Direct port of the iOS `AuthInterceptor.adapt` — reads the store per request so a token
 * rotated by a refresh is picked up automatically on the retry.
 */
internal class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): OkHttpResponse {
        val original = chain.request()
        val token = tokenStore.accessToken
        if (token.isNullOrEmpty()) return chain.proceed(original)
        val type = tokenStore.tokenType?.takeIf { it.isNotEmpty() } ?: "Bearer"
        val authed = original.newBuilder()
            .header("Authorization", "$type $token")
            .build()
        return chain.proceed(authed)
    }
}

class ApiClient internal constructor(
    val environment: ApiEnvironment,
    val tokenStore: TokenStore,
) {

    @PublishedApi
    internal val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    /**
     * Bare client: NO auth header. Used for the refresh endpoint (so the refresh call is
     * never itself intercepted/retried) and for external calls (S3, ViaCEP). Mirrors the
     * iOS use of `URLSession.shared` for refresh.
     */
    internal val rawClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /** Authenticated client used by every regular endpoint. */
    private val authClient: OkHttpClient = rawClient.newBuilder()
        .addInterceptor(AuthInterceptor(tokenStore))
        .build()

    private val service: ApiService = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(environment.baseUrl))
        .client(authClient)
        .build()
        .create()

    private val refresher = TokenRefresher(tokenStore) { rt -> refreshTokensNow(rt) != null }

    fun clearTokens() = tokenStore.clear()

    /**
     * Performs a single refresh against the refresh endpoint using the bare client.
     * iOS parity: a non-2xx response means the refresh token is dead -> clear the store so
     * the app falls into login; a transport error returns null WITHOUT clearing (transient).
     *
     * All store mutations are COMPARE-AND-SET against [refreshToken]: we only write/clear if
     * the store still holds the exact token this call started with. A concurrent login (or
     * another refresh) may replace the session while this request is in flight; without the
     * guard a stale success would clobber the newer session, or a stale failure would clear a
     * valid one — both kick the user to login.
     */
    internal suspend fun refreshTokensNow(refreshToken: String): BearerTokensLite? {
        val request = Request.Builder()
            .url(joinUrl(environment.baseUrl, REFRESH_PATH))
            .post(
                json.encodeToString(RefreshRequestDto.serializer(), RefreshRequestDto(refreshToken))
                    .toRequestBody(JSON_MEDIA)
            )
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build()

        val response = runCatching { rawClient.newCall(request).await() }.getOrElse { return null }
        return response.use { resp ->
            if (!resp.isSuccessful) {
                // Only clear if THIS token is the one still in the store (it's the dead one).
                // runCatching: EncryptedSharedPreferences writes can throw on keystore failure.
                if (tokenStore.refreshToken == refreshToken) {
                    runCatching { tokenStore.clear() }
                }
                return null
            }
            val text = resp.body?.string() ?: return null
            runCatching {
                val parsed = json.decodeFromString(RefreshResponseDto.serializer(), text)
                // Don't clobber a newer session written while this refresh was in flight.
                if (tokenStore.refreshToken == refreshToken) {
                    tokenStore.accessToken = parsed.resultado.accessToken
                    tokenStore.refreshToken = parsed.resultado.refreshToken
                    if (tokenStore.tokenType.isNullOrEmpty()) tokenStore.tokenType = "Bearer"
                }
                BearerTokensLite(parsed.resultado.accessToken, parsed.resultado.refreshToken)
            }.getOrNull()
        }
    }

    suspend inline fun <reified T> send(
        endpoint: Endpoint,
        body: Any? = null,
        query: Map<String, Any?>? = null,
    ): T {
        val text = execute(endpoint, body, query, allowEmpty = false)
        return try {
            json.decodeFromString(text)
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Decoding(e)
        }
    }

    suspend fun sendVoid(
        endpoint: Endpoint,
        body: Any? = null,
        query: Map<String, Any?>? = null,
    ) {
        execute(endpoint, body, query, allowEmpty = true)
    }

    /**
     * Runs the request and applies the iOS auto-refresh-and-retry-once policy:
     *  - capture the access token used for this attempt (`previousToken`)
     *  - on an auth error (HTTP 401 OR API `erro=6`) that is not the refresh endpoint and
     *    there is a refresh token, ask the single-flight [TokenRefresher] to renew
     *  - if renewed, replay the request exactly once (the AuthInterceptor adds the new token)
     */
    @PublishedApi
    internal suspend fun execute(
        endpoint: Endpoint,
        body: Any?,
        query: Map<String, Any?>?,
        allowEmpty: Boolean,
    ): String {
        val previousToken = tokenStore.accessToken
        val (status, text) = perform(endpoint, body, query)
        if (status < 400) return successBody(text, allowEmpty)

        val err = parseApiError(text, status)
        // Token expirado/inválido: HTTP 401, ou os códigos de negócio 6 ("token
        // expirado") e 92 ("Token inválido"). Ambos disparam o refresh + replay.
        val isAuthError = status == 401 || (err is ApiError.Api && (err.code == 6 || err.code == 92))
        val isRefreshEndpoint = endpoint.path.contains(REFRESH_PATH)
        val canRefreshRetry = isAuthError && !isRefreshEndpoint && !tokenStore.refreshToken.isNullOrEmpty()
        if (!canRefreshRetry) throw err

        if (!refresher.refreshIfNeeded(previousToken)) throw err

        val (retryStatus, retryText) = perform(endpoint, body, query)
        if (retryStatus < 400) return successBody(retryText, allowEmpty)
        throw parseApiError(retryText, retryStatus)
    }

    private fun successBody(text: String?, allowEmpty: Boolean): String =
        text ?: if (allowEmpty) "" else throw ApiError.Unknown

    /** Issues one HTTP attempt and returns (statusCode, bodyText). */
    private suspend fun perform(
        endpoint: Endpoint,
        body: Any?,
        query: Map<String, Any?>?,
    ): Pair<Int, String?> {
        val url = buildUrl(environment.baseUrl, endpoint.path, query)
        val headers = endpoint.headers
        val response: Response<ResponseBody> = try {
            when (endpoint.method) {
                HttpMethod.Get -> service.get(url, headers)
                HttpMethod.Post -> if (body == null) service.post(url, headers) else service.post(url, headers, body.toJsonBody())
                HttpMethod.Put -> if (body == null) service.put(url, headers) else service.put(url, headers, body.toJsonBody())
                HttpMethod.Patch -> if (body == null) service.patch(url, headers) else service.patch(url, headers, body.toJsonBody())
                HttpMethod.Delete -> if (body == null) service.delete(url, headers) else service.deleteWithBody(url, headers, body.toJsonBody())
                HttpMethod.Head, HttpMethod.Options ->
                    throw ApiError.InvalidRequest("Unsupported method ${endpoint.method}")
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: Throwable) {
            throw ApiError.Transport(e)
        }
        val code = response.code()
        val payload = (if (response.isSuccessful) response.body() else response.errorBody())?.string()
        return code to payload
    }

    /**
     * Serializes a request body via reflective serializer lookup on its runtime class — the
     * same contract as the original Ktor `setBody(Any?)`. Bodies must be concrete
     * `@Serializable` types (every endpoint here passes a data class), not bare generic
     * collections, since the erased runtime class loses type arguments.
     */
    private fun Any.toJsonBody(): RequestBody {
        @Suppress("UNCHECKED_CAST")
        val ser = serializer(this.javaClass) as KSerializer<Any>
        return json.encodeToString(ser, this).toRequestBody(JSON_MEDIA)
    }

    private fun buildUrl(base: String, path: String, query: Map<String, Any?>?): String {
        val full = joinUrl(base, path)
        if (query.isNullOrEmpty()) return full
        val builder = full.toHttpUrl().newBuilder()
        query.forEach { (key, value) -> if (value != null) builder.addQueryParameter(key, value.toString()) }
        return builder.build().toString()
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

    private companion object {
        const val TIMEOUT_SECONDS = 905L
    }
}

/** Awaits an OkHttp [Call] without blocking the calling coroutine. */
internal suspend fun Call.await(): OkHttpResponse = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { runCatching { cancel() } }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (!cont.isCancelled) cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: OkHttpResponse) {
            // If the caller was already cancelled, resume() is a no-op and the body would
            // leak its connection — close it. The onCancellation lambda covers the race
            // where cancellation lands between the isActive check and resume.
            if (cont.isActive) {
                cont.resume(response) { _, _, _ -> runCatching { response.close() } }
            } else {
                runCatching { response.close() }
            }
        }
    })
}

internal fun joinUrl(baseUrl: String, path: String): String {
    val cleanBase = baseUrl.trimEnd('/')
    val cleanPath = path.removePrefix("/")
    return "$cleanBase/$cleanPath"
}

private fun ensureTrailingSlash(url: String): String = if (url.endsWith("/")) url else "$url/"

internal data class BearerTokensLite(val accessToken: String, val refreshToken: String)

@Serializable
private data class RefreshRequestDto(val refreshToken: String)

@Serializable
private data class RefreshResponseDto(val resultado: Resultado) {
    @Serializable
    data class Resultado(val accessToken: String, val refreshToken: String)
}
