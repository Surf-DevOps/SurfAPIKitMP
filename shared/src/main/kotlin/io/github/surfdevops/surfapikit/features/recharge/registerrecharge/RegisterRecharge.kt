package io.github.surfdevops.surfapikit.features.recharge.registerrecharge

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class RegisterRechargeRequest(val nuMsisdn: String, val nuPlano: String)

@Serializable
data class RegisterRechargeSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: RechargeData,
    val transaction: TransactionInfo? = null
) {
    @Serializable
    data class RechargeData(
        val coRecarga: String,
        val nuMsisdn: Long,
        val stRecarga: String,
        val dtCadastro: String
    )

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal object RegisterRechargeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recarga/registrar"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.registerRecharge(request: RegisterRechargeRequest): RegisterRechargeSuccess =
    client.send(RegisterRechargeEndpoint, body = request)
