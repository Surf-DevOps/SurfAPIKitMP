package io.github.surfdevops.surfapikit

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import io.github.surfdevops.surfapikit.core.ApiClient
import io.github.surfdevops.surfapikit.core.TokenStore
import io.github.surfdevops.surfapikit.core.createPlatformTokenStore
import io.github.surfdevops.surfapikit.features.authentication.refreshtoken.RefreshTokenRequest
import io.github.surfdevops.surfapikit.features.authentication.refreshtoken.refreshTokenInternal
import io.ktor.client.plugins.auth.providers.BearerTokens

object SurfApiKit {

    @Volatile
    private var environment: ApiEnvironment = ApiEnvironment.PRODUCTION

    @Volatile
    private var tokenStoreOverride: TokenStore? = null

    private val lock = Any()

    @Volatile
    private var _client: ApiClient? = null

    val client: ApiClient
        get() = _client ?: synchronized(lock) {
            _client ?: build().also { _client = it }
        }

    val tokenStore: TokenStore get() = client.tokenStore

    fun configure(environment: ApiEnvironment = this.environment, tokenStore: TokenStore? = null) {
        synchronized(lock) {
            this.environment = environment
            this.tokenStoreOverride = tokenStore
            _client = build()
        }
    }

    fun clearTokens() {
        client.clearTokens()
    }

    private fun build(): ApiClient {
        val store = tokenStoreOverride ?: createPlatformTokenStore()
        return ApiClient(
            environment = environment,
            tokenStore = store,
            refreshHandler = { refresh ->
                runCatching {
                    val resp = SurfApiKit.refreshTokenInternal(RefreshTokenRequest(refreshToken = refresh))
                    BearerTokens(
                        accessToken = resp.resultado.accessToken,
                        refreshToken = resp.resultado.refreshToken
                    )
                }.getOrNull()
            }
        )
    }
}
