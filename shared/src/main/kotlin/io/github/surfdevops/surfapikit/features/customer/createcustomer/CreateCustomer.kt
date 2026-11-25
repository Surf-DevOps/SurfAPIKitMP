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

/**
 * Body do `v4/customer/create-customer`. Chip PROVISIONED: igual ao v3. Chip NOT USED: vai
 * junto o plano escolhido ([nuPlano]) e o cartão ([card]); o backend cobra o plano, ativa o
 * chip com a recarga paga e, com [isRecurring], agenda a recarga programada nesse cartão.
 */
@Serializable
data class CreateCustomerV4Request(
    val nuIccid: String,
    val nuDDD: Int,
    val dsNome: String,
    val nuDocumento: String,
    val dsPassword: String,
    val noMvno: String,
    val noPerfil: String? = null,
    val nuDocumentoResponsavel: String? = null,
    val nuPlano: String? = null,
    val card: Card? = null,
    val isRecurring: Boolean? = null,
) {
    @Serializable
    data class Card(
        val holderName: String,
        val cardNumber: String,
        /** MM */
        val expiryMonth: String,
        /** YY ou YYYY */
        val expiryYear: String,
        val cvv: String,
    ) {
        override fun toString(): String = "Card(final=${cardNumber.takeLast(4)})"
    }

    override fun toString(): String = "CreateCustomerV4Request(nuIccid=$nuIccid, nuPlano=$nuPlano)"
}

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
        val tokenType: String,
        /** Só na ativação do NOT USED com compra: a recarga programada foi agendada. */
        val recargaProgramada: Boolean? = null,
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
    /** O backend não conseguiu ativar o chip (com compra: o pagamento foi estornado ou fica para retomada). */
    const val ACTIVATION_FAILED = 156
    /** Chip NOT USED sem plano e cartão. */
    const val PLAN_REQUIRED = 159
    /** Plano fora do catálogo do chip. */
    const val PLAN_NOT_AVAILABLE = 160
    /** Catálogo indisponível; nada foi cobrado. */
    const val PLAN_LOOKUP_UNAVAILABLE = 161
    /** Cartão recusado; nada foi cobrado nem ativado. */
    const val PAYMENT_DECLINED = 162
    /** Já existe uma ativação em andamento para o ICCID. */
    const val ACTIVATION_IN_PROGRESS = 163
    /** Ativação indisponível por configuração do backend; bloqueada antes de cobrar. */
    const val ACTIVATION_UNAVAILABLE = 164
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

/** Ativação com compra do plano (chip `NOT USED`). Ver [CreateCustomerV4Request]. */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.createCustomerV4(request: CreateCustomerV4Request): CreateCustomerSuccess {
    val response: CreateCustomerSuccess = client.send(CreateCustomerV4Endpoint, body = request)
    tokenStore.accessToken = response.resultado.accessToken
    tokenStore.tokenType = response.resultado.tokenType
    tokenStore.refreshToken = response.resultado.refreshToken
    return response
}

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
