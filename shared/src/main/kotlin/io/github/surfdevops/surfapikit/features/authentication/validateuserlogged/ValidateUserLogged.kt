package io.github.surfdevops.surfapikit.features.authentication.validateuserlogged

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class ValidateUserLoggedRequest(
    /** Identificador da linha usado na rota `GET /v2/auth/validate/:userIdentifier` (coMsisdn). */
    val userIdentifier: String
) {
    companion object {
        /** Compatibilidade: o identificador da rota é o coMsisdn da linha. */
        fun fromNuMsisdn(nuMsisdn: String): ValidateUserLoggedRequest =
            ValidateUserLoggedRequest(userIdentifier = nuMsisdn)
    }
}

@Serializable
data class ValidateUserLoggedSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val isLoggedIn: Boolean,
        val userIdentifier: String
    )
}

internal data class ValidateUserLoggedEndpoint(val userIdentifier: String) : Endpoint {
    override val path: String = "spec-mobile/v2/auth/validate/$userIdentifier"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.validateUserLogged(request: ValidateUserLoggedRequest): ValidateUserLoggedSuccess =
    client.send(ValidateUserLoggedEndpoint(request.userIdentifier))
