package io.github.surfdevops.surfapikit.features.recharge.registerschedulerecharge

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class RegisterScheduleRechargeRequest(
    val nuMsisdn: String,
    val nuPlano: String,
    val dtProgramada: String
)

@Serializable
data class RegisterScheduleRechargeSuccess(
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

internal object RegisterScheduleRechargeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recarga/registar-programada"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.registerScheduleRecharge(
    request: RegisterScheduleRechargeRequest
): RegisterScheduleRechargeSuccess =
    client.send(RegisterScheduleRechargeEndpoint, body = request)
