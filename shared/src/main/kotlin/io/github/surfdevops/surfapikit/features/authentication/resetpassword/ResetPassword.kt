package io.github.surfdevops.surfapikit.features.authentication.resetpassword

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

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

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.resetPassword(request: ResetPasswordRequest): ResetPasswordSuccess =
    client.send(
        ResetPasswordEndpoint(request.documentId),
        body = ResetPasswordBody(request.dsPassword, request.coMvno)
    )
