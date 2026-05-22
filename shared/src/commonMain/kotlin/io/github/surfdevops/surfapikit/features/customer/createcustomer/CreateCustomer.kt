package io.github.surfdevops.surfapikit.features.customer.createcustomer

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class CreateCustomerRequest(
    val nuIccid: String,
    val nuDDD: Int,
    val dsNome: String,
    val nuDocumento: String,
    val dsPassword: String,
    val noMvno: String,
    val noPerfil: String? = null,
    val nuDocumentoResponsavel: String? = null
)

@Serializable
data class CreateCustomerSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: CreateCustomerResultado,
    val transaction: CreateCustomerTransaction
) {
    @Serializable
    data class CreateCustomerResultado(
        val customerID: String,
        val profileID: String,
        val coMsisdn: Long,
        val nuMsisdn: Long,
        val nuDocumento: String,
        val nuPlano: String? = null,
        val noPlano: String? = null,
        val activationCode: String,
        val accessToken: String,
        val refreshToken: String,
        val dtExpiracao: String,
        val dtExpiracaoRefresh: String,
        val tokenType: String
    )

    @Serializable
    data class CreateCustomerTransaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal object CreateCustomerEndpoint : Endpoint {
    override val path = "spec-mobile/v3/customer/create-customer"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createCustomer(request: CreateCustomerRequest): CreateCustomerSuccess {
    val response: CreateCustomerSuccess = client.send(CreateCustomerEndpoint, body = request)
    tokenStore.accessToken = response.resultado.accessToken
    tokenStore.tokenType = response.resultado.tokenType
    tokenStore.refreshToken = response.resultado.refreshToken
    return response
}
