package io.github.surfdevops.surfapikit.features.config

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.platform.AppInfo
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class VersionVerifySuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String? = null,
    val resultado: VersionVerifyConfigResult
) {
    @Serializable
    data class VersionVerifyConfigResult(
        val name: String,
        val minVersion: String,
        val forceUpdate: Boolean
    )
}

internal data class VersionVerifyEndpoint(val coMvno: String) : Endpoint {
    override val path: String = "spec-mobile/v1/config/$coMvno"
    override val method = HttpMethod.Get
    override val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json",
        "x-app-version" to AppInfo.appVersion,
        "x-platform" to AppInfo.platform
    )
}

suspend fun SurfApiKit.getVersionVerify(coMvno: String): VersionVerifySuccess =
    client.send(VersionVerifyEndpoint(coMvno))
