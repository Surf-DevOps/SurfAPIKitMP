package io.github.surfdevops.surfapikit.features.customer.createcustomer

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
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

/**
 * Cadastro do chip `NOT USED`: o spec-mobile ativa a linha e cria o cliente na mesma chamada.
 * O body é o mesmo do v3, e o `nuDDD` precisa ser o DDD do `nuMsisdn` do chip.
 */
internal object CreateCustomerV4Endpoint : Endpoint {
    override val path = "spec-mobile/v4/customer/create-customer"
    override val method = HttpMethod.Post
}

/** Códigos de [ApiError.Api] do `v4/customer/create-customer` vistos em prod (06/11/2026). */
object CreateCustomerV4Error {
    /** ICCID inválido ou não encontrado. */
    const val ICCID_NOT_FOUND = 150
    /** O `nuDDD` não corresponde ao número do chip. */
    const val DDD_MISMATCH = 153
    /** O backend não conseguiu ativar o chip. */
    const val ACTIVATION_FAILED = 156
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createCustomer(request: CreateCustomerRequest): CreateCustomerSuccess =
    sendCreateCustomer(CreateCustomerEndpoint, request)

/**
 * Ativa o chip `NOT USED` e cria o cliente. A resposta de sucesso segue a do v3 até o backend
 * confirmar o formato. Os erros conhecidos estão em [CreateCustomerV4Error].
 */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createCustomerV4(request: CreateCustomerRequest): CreateCustomerSuccess =
    sendCreateCustomer(CreateCustomerV4Endpoint, request)

private suspend fun SurfApiKit.sendCreateCustomer(
    endpoint: Endpoint,
    request: CreateCustomerRequest,
): CreateCustomerSuccess {
    val response: CreateCustomerSuccess = client.send(endpoint, body = request)
    tokenStore.accessToken = response.resultado.accessToken
    tokenStore.tokenType = response.resultado.tokenType
    tokenStore.refreshToken = response.resultado.refreshToken
    return response
}
