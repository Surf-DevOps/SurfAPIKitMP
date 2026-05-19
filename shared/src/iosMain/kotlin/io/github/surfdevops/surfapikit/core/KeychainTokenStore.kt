package io.github.surfdevops.surfapikit.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CFBridgingRelease
import kotlinx.cinterop.CFBridgingRetain
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.OSStatus
import platform.darwin.noErr

private const val SERVICE = "io.github.surfdevops.surfapikit"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainTokenStore : TokenStore {

    private val _authChanges = MutableStateFlow(0L)
    override val authChanges: StateFlow<Long> = _authChanges.asStateFlow()

    override var selectionToken: String?
        get() = read("selectionToken")
        set(value) = write("selectionToken", value)

    override var accessToken: String?
        get() = read("accessToken")
        set(value) {
            write("accessToken", value)
            _authChanges.value = _authChanges.value + 1
        }

    override var refreshToken: String?
        get() = read("refreshToken")
        set(value) = write("refreshToken", value)

    override var tokenType: String?
        get() = read("tokenType")
        set(value) {
            write("tokenType", value)
            _authChanges.value = _authChanges.value + 1
        }

    private fun baseQuery(account: String): Map<Any?, Any?> = mapOf<Any?, Any?>(
        kSecClass to kSecClassGenericPassword,
        kSecAttrService to SERVICE,
        kSecAttrAccount to account
    )

    private fun read(account: String): String? = memScoped {
        val query = (baseQuery(account) + mapOf(
            kSecMatchLimit to kSecMatchLimitOne,
            kSecReturnData to kCFBooleanTrue
        )).toCFDictionary()

        val result = allocPointerTo<CFTypeRefVar>()
        val status: OSStatus = SecItemCopyMatching(query, result.reinterpret())
        if (status != noErr) return@memScoped null
        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding) as String?
    }

    private fun write(account: String, value: String?) = memScoped {
        if (value == null) {
            SecItemDelete(baseQuery(account).toCFDictionary())
            return@memScoped
        }
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return@memScoped
        val attrs = mapOf<Any?, Any?>(
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlock
        )
        val status = SecItemUpdate(baseQuery(account).toCFDictionary(), attrs.toCFDictionary())
        if (status != noErr) {
            SecItemAdd((baseQuery(account) + attrs).toCFDictionary(), null)
        }
    }

    private fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef {
        val nsDict = platform.Foundation.NSMutableDictionary()
        forEach { (k, v) -> nsDict.setObject(v!!, k as Any) }
        @Suppress("UNCHECKED_CAST")
        return CFBridgingRetain(nsDict) as CFDictionaryRef
    }
}

actual fun createPlatformTokenStore(): TokenStore = KeychainTokenStore()
