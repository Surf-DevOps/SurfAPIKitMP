package io.github.surfdevops.surfapikit.features.customer.getcustomer

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class GetCustomerRequest(val coMsisdn: String)

@Serializable
data class GetCustomerSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: CustomerResult? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class CustomerResult(
        val customerID: String,
        val coMsisdn: Long? = null,
        val nuMsisdn: Long,
        val coMvno: Int? = null,
        val profileID: String,
        val dsNome: String,
        val nuDocumento: String,
        val tpDocumento: String? = null,
        val dsPassword: String,
        val dsEmail: String? = null,
        val nuMsisdnAlternativo: Int? = null,
        val dsCEP: String? = null,
        val dsEndereco: String? = null,
        val dsNumero: Int? = null,
        val dsComplemento: String? = null,
        val dsBairro: String? = null,
        val dsCidade: String? = null,
        val dsEstado: String? = null,
        val stUsuario: String? = null,
        val dtCadastro: String? = null,
        val dtAlteracao: String
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal data class GetCustomerEndpoint(val coMsisdn: String) : Endpoint {
    override val path: String = "spec-mobile/v1/customer/get-customer/$coMsisdn"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getCustomer(request: GetCustomerRequest): GetCustomerSuccess =
    client.send(GetCustomerEndpoint(request.coMsisdn))
