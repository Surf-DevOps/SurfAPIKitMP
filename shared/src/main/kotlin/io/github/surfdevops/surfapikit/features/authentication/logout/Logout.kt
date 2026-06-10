package io.github.surfdevops.surfapikit.features.authentication.logout

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class LogoutSuccess(
    val sucesso: Int,
    val mensagem: String
)

internal object LogoutEndpoint : Endpoint {
    override val path = "spec-mobile/v2/auth/logout"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.logout(): LogoutSuccess = client.send(LogoutEndpoint)
