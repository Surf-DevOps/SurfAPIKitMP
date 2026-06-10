package io.github.surfdevops.surfapikit

import io.github.surfdevops.surfapikit.config.ApiEnvironment
import io.github.surfdevops.surfapikit.core.ApiClient
import io.github.surfdevops.surfapikit.core.TokenStore
import io.github.surfdevops.surfapikit.core.createPlatformTokenStore
import kotlin.concurrent.Volatile

object SurfApiKit {

    @Volatile
    private var environment: ApiEnvironment = ApiEnvironment.PRODUCTION

    @Volatile
    private var tokenStoreOverride: TokenStore? = null

    @Volatile
    private var _client: ApiClient? = null

    val client: ApiClient
        get() = _client ?: build().also { _client = it }

    val tokenStore: TokenStore get() = client.tokenStore

    fun configure(environment: ApiEnvironment = this.environment, tokenStore: TokenStore? = null) {
        this.environment = environment
        this.tokenStoreOverride = tokenStore
        _client = build()
    }

    fun clearTokens() {
        client.clearTokens()
    }

    private fun build(): ApiClient {
        val store = tokenStoreOverride ?: createPlatformTokenStore()
        return ApiClient(environment = environment, tokenStore = store)
    }
}
