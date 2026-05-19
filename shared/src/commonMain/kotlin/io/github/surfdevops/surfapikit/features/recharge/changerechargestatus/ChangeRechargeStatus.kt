package io.github.surfdevops.surfapikit.features.recharge.changerechargestatus

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

data class ChangeRechargeStatusRequest(
    val coRecarga: String,
    val customerID: String,
    val noMetodo: String,
    val stPagamento: String,
    val nuTransacao: String
)

@Serializable
internal data class ChangeRechargeStatusBody(
    val customerID: String,
    val noMetodo: String,
    val stPagamento: String,
    val nuTransacao: String
)

@Serializable
data class ChangeRechargeStatusSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: StatusChangeData? = null,
    val transaction: TransactionData? = null
) {
    @Serializable
    data class StatusChangeData(val nuMsisdn: String, val dtRecarga: String)

    @Serializable
    data class TransactionData(val localTransactionId: String, val localTransactionDate: String)
}

internal object ChangeRechargeStatusEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recarga/alterar-recarga"
    override val method = HttpMethod.Put
}

suspend fun SurfApiKit.changeRechargeStatus(request: ChangeRechargeStatusRequest): ChangeRechargeStatusSuccess =
    client.send(
        ChangeRechargeStatusEndpoint,
        body = ChangeRechargeStatusBody(request.customerID, request.noMetodo, request.stPagamento, request.nuTransacao),
        query = mapOf("coRecarga" to request.coRecarga)
    )
