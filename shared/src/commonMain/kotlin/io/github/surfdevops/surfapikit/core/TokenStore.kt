package io.github.surfdevops.surfapikit.core

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
    @Volatile override var accessToken: String? = null
        set(value) {
            field = value
            _authChanges.value = _authChanges.value + 1
        }
    @Volatile override var refreshToken: String? = null
    @Volatile override var tokenType: String? = null
        set(value) {
            field = value
            _authChanges.value = _authChanges.value + 1
        }

    private val _authChanges = MutableStateFlow(0L)
    override val authChanges: StateFlow<Long> = _authChanges.asStateFlow()
}

expect fun createPlatformTokenStore(): TokenStore
