package io.github.surfdevops.surfapikit.features.customer.createaccess

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccessRequest(
    val nuMsisdn: Long,
    val dsNome: String,
    val dsPassword: String,
    val companyID: String
)

@Serializable
data class CreateAccessSuccess(
    val sucesso: Int,
    val descricao: String,
    val resultado: AccessResult,
    val transaction: Transaction
) {
    @Serializable
    data class AccessResult(
        val customerID: String,
        val profileID: String,
        val coMsisdn: Long,
        val nuMsisdn: Long,
        val nuDocumento: String,
        val accessToken: String,
        val refreshToken: String,
        val dtExpiracao: String,
        val dtExpiracaoRefresh: String
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal object CreateAccessEndpoint : Endpoint {
    override val path = "spec-mobile/v3/customer/create-access"
    override val method = HttpMethod.Post
}

suspend fun SurfApiKit.createAccess(request: CreateAccessRequest): CreateAccessSuccess {
    val response: CreateAccessSuccess = client.send(CreateAccessEndpoint, body = request)
    tokenStore.accessToken = response.resultado.accessToken
    tokenStore.refreshToken = response.resultado.refreshToken
    return response
}
