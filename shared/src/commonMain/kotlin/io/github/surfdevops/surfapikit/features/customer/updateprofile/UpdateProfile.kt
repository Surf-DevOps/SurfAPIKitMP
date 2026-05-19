package io.github.surfdevops.surfapikit.features.customer.updateprofile

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UpdateProfileRequest(
    @Transient val customerID: String = "",
    val dsEmail: String,
    val nuMsisdnAlternativo: Long,
    val dsCEP: String,
    val dsEndereco: String,
    val dsNumero: Int,
    val dsComplemento: String,
    val dsBairro: String,
    val dsCidade: String,
    val dsEstado: String
)

@Serializable
data class UpdateProfileSuccess(
    val sucesso: Int,
    val descricao: String,
    val transacao: String,
    val resultado: ProfileData? = null,
    val transaction: TransactionData? = null
) {
    @Serializable
    data class ProfileData(
        val coMsisdn: Int? = null,
        val nuMsisdn: Long? = null,
        val coMvno: Int? = null,
        val nuDocumento: String? = null,
        val dsEmail: String? = null,
        val tpDocumento: String? = null,
        val nuMsisdnAlternativo: Long? = null,
        val dsEndereco: String? = null,
        val dsComplemento: String? = null,
        val dsBairro: String? = null,
        val dsCidade: String? = null,
        val dsEstado: String? = null,
        val stUsuario: String? = null,
        val dtCadastro: String? = null,
        val dtAlteracao: String? = null,
        val customerID: String? = null,
        val dsCEP: String? = null,
        val dsNumero: Int? = null,
        val dsNome: String? = null,
        val profileID: String? = null,
        val dsPassword: String? = null,
        val noPerfil: String? = null,
        val nuDocumentoResponsavel: String? = null,
        val customerData: String? = null
    )

    @Serializable
    data class TransactionData(
        val globalTransactionId: String,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal data class UpdateProfileEndpoint(val customerID: String) : Endpoint {
    override val path: String = "spec-mobile/v1/customer/update-customer/$customerID"
    override val method = HttpMethod.Patch
}

suspend fun SurfApiKit.updateProfile(request: UpdateProfileRequest): UpdateProfileSuccess =
    client.send(UpdateProfileEndpoint(request.customerID), body = request)
