package io.github.surfdevops.surfapikit.core

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.surfdevops.surfapikit.platform.AppContextHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "io.github.surfdevops.surfapikit.tokens"

class EncryptedTokenStore(context: Context) : TokenStore {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _authChanges = MutableStateFlow(0L)
    override val authChanges: StateFlow<Long> = _authChanges.asStateFlow()

    override var selectionToken: String?
        get() = prefs.getString("selectionToken", null)
        set(value) = prefs.edit().apply { if (value == null) remove("selectionToken") else putString("selectionToken", value) }.apply()

    override var accessToken: String?
        get() = prefs.getString("accessToken", null)
        set(value) {
            prefs.edit().apply { if (value == null) remove("accessToken") else putString("accessToken", value) }.apply()
            _authChanges.value = _authChanges.value + 1
        }

    override var refreshToken: String?
        get() = prefs.getString("refreshToken", null)
        set(value) = prefs.edit().apply { if (value == null) remove("refreshToken") else putString("refreshToken", value) }.apply()

    override var tokenType: String?
        get() = prefs.getString("tokenType", null)
        set(value) {
            prefs.edit().apply { if (value == null) remove("tokenType") else putString("tokenType", value) }.apply()
            _authChanges.value = _authChanges.value + 1
        }
}

fun createPlatformTokenStore(): TokenStore =
    EncryptedTokenStore(AppContextHolder.context)
