package io.github.surfdevops.surfapikit.features.customer.customerprofile

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CustomerProfileRequest(
    val nuDocumento: String,
    val coMvno: Int
)

@Serializable
data class CustomerProfileSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: CustomerProfileResultado
)

@Serializable
data class CustomerProfileResultado(
    // "ESTUDANTE"/"PROFESSOR"/"COLABORADOR" quando o CPF existe na base de beneficiários,
    // ou null quando o CPF não foi encontrado.
    val noPerfil: String? = null
)

internal object CustomerProfileEndpoint : Endpoint {
    override val path = "spec-mobile/v2/customer/perfil"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getCustomerProfile(request: CustomerProfileRequest): CustomerProfileSuccess =
    client.send(
        CustomerProfileEndpoint,
        query = mapOf("nuDocumento" to request.nuDocumento, "coMvno" to request.coMvno.toString())
    )
