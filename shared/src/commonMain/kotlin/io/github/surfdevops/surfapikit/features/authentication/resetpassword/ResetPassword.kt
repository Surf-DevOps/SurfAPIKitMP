package io.github.surfdevops.surfapikit.features.authentication.resetpassword

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

data class ResetPasswordRequest(
    val documentId: String,
    val dsPassword: String,
    val coMvno: Int
)

@Serializable
internal data class ResetPasswordBody(
    val dsPassword: String,
    val coMvno: Int
)

@Serializable
data class ResetPasswordSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String
)

internal data class ResetPasswordEndpoint(val documentId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/customer/reset-password/$documentId"
    override val method = HttpMethod.Patch
}

suspend fun SurfApiKit.resetPassword(request: ResetPasswordRequest): ResetPasswordSuccess =
    client.send(
        ResetPasswordEndpoint(request.documentId),
        body = ResetPasswordBody(request.dsPassword, request.coMvno)
    )
