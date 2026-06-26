package io.github.surfdevops.surfapikit.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordinates automatic access-token renewal after a request gets a 401.
 *
 * Faithful Kotlin port of the native iOS `TokenRefresher` actor. Guarantees that multiple
 * requests failing at the same time share a SINGLE refresh instead of firing several in
 * parallel — the bug that poisoned the Android session: the refresh token is single-use, so
 * two concurrent refreshes over the same token meant the loser got a >= 400 and the SDK
 * cleared the freshly-written valid pair (logout -> login -> reopen -> login).
 *
 * Two guards, both mirroring iOS:
 *  1. `previousToken` — if the access token already changed since this request was sent,
 *     another request already refreshed; reuse it, don't spend the refresh token again.
 *  2. A shared in-flight [Deferred] (iOS's `currentTask`): concurrent callers await the SAME
 *     refresh and get the SAME result. The refresh runs on an SDK-owned [scope], NOT the
 *     caller's coroutine, so a single caller being cancelled (screen closed, nav away) cannot
 *     abandon a rotation the other waiters depend on.
 */
internal class TokenRefresher(
    private val tokenStore: TokenStore,
    /** Performs one real refresh. Returns true on success (tokens rotated and stored). */
    private val performRefresh: suspend (refreshToken: String) -> Boolean,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var inFlight: Deferred<Boolean>? = null

    /**
     * @param previousToken the access token used by the request that received the 401.
     * @return true if there is a fresh, valid access token to retry the request with.
     */
    suspend fun refreshIfNeeded(previousToken: String?): Boolean {
        // Someone already rotated the token while this request was in flight.
        if (alreadyRefreshed(previousToken)) return true

        val job = mutex.withLock {
            // Re-check inside the lock: a concurrent 401 may have refreshed while we waited.
            if (alreadyRefreshed(previousToken)) return true

            // Join a refresh already running for this burst instead of starting another.
            inFlight?.let { if (it.isActive) return@withLock it }

            val refreshToken = tokenStore.refreshToken
            if (refreshToken.isNullOrEmpty()) {
                // Only clear if there is something to clear, so we don't emit a spurious
                // authChanges event (TokenSync and other observers collect it). runCatching:
                // EncryptedSharedPreferences writes can throw on a keystore failure.
                if (tokenStore.accessToken != null || tokenStore.refreshToken != null) {
                    runCatching { tokenStore.clear() }
                }
                return false
            }

            // Detached from the caller: completes even if this caller is cancelled. The body
            // never throws (any failure -> false), so awaiters always get a clean Boolean and
            // a keystore/parse error can't propagate a non-ApiError to every waiter.
            scope.async { runCatching { performRefresh(refreshToken) }.getOrDefault(false) }
                .also { inFlight = it }
        }

        // Awaiting is cancellable per-caller; the shared job keeps running for the others.
        // inFlight is intentionally not nulled on completion: the isActive gate above is the
        // sole liveness signal (a finished Deferred is never joined), matching join semantics
        // without needing a suspended reset under the lock.
        return job.await()
    }

    // A null previousToken with any non-empty token present short-circuits to true by design:
    // a token appeared since this request was sent, so retry with it instead of refreshing.
    private fun alreadyRefreshed(previousToken: String?): Boolean {
        val current = tokenStore.accessToken
        return !current.isNullOrEmpty() && current != previousToken
    }
}
