package io.github.surfdevops.surfapikit.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

private const val PREFIX = "io.github.surfdevops.surfapikit."

class IosTokenStore : TokenStore {

    private val defaults = NSUserDefaults.standardUserDefaults

    private val _authChanges = MutableStateFlow(0L)
    override val authChanges: StateFlow<Long> = _authChanges.asStateFlow()

    override var selectionToken: String?
        get() = defaults.stringForKey(PREFIX + "selectionToken")
        set(value) = set(PREFIX + "selectionToken", value)

    override var accessToken: String?
        get() = defaults.stringForKey(PREFIX + "accessToken")
        set(value) {
            set(PREFIX + "accessToken", value)
            _authChanges.value = _authChanges.value + 1
        }

    override var refreshToken: String?
        get() = defaults.stringForKey(PREFIX + "refreshToken")
        set(value) = set(PREFIX + "refreshToken", value)

    override var tokenType: String?
        get() = defaults.stringForKey(PREFIX + "tokenType")
        set(value) {
            set(PREFIX + "tokenType", value)
            _authChanges.value = _authChanges.value + 1
        }

    private fun set(key: String, value: String?) {
        if (value == null) defaults.removeObjectForKey(key) else defaults.setObject(value, forKey = key)
    }
}

actual fun createPlatformTokenStore(): TokenStore = IosTokenStore()
