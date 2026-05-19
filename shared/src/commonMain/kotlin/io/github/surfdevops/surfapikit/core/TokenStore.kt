package io.github.surfdevops.surfapikit.core

import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStore {
    var selectionToken: String?
    var accessToken: String?
    var refreshToken: String?
    var tokenType: String?

    fun clear() {
        selectionToken = null
        accessToken = null
        refreshToken = null
        tokenType = null
    }

    val authChanges: StateFlow<Long>
}

class InMemoryTokenStore : TokenStore {
    @Volatile override var selectionToken: String? = null
    @Volatile private var _accessToken: String? = null
    @Volatile override var refreshToken: String? = null
    @Volatile private var _tokenType: String? = null

    override var accessToken: String?
        get() = _accessToken
        set(value) {
            _accessToken = value
            _authChanges.value = _authChanges.value + 1
        }

    override var tokenType: String?
        get() = _tokenType
        set(value) {
            _tokenType = value
            _authChanges.value = _authChanges.value + 1
        }

    private val _authChanges = MutableStateFlow(0L)
    override val authChanges: StateFlow<Long> = _authChanges.asStateFlow()
}

expect fun createPlatformTokenStore(): TokenStore
