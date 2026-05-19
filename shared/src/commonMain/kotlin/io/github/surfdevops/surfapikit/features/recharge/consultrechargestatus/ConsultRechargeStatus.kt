package io.github.surfdevops.surfapikit.features.recharge.consultrechargestatus

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

data class ConsultRechargeStatusRequest(val coRecarga: String)

@Serializable
data class ConsultRechargeStatusSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: RechargeStatus? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class RechargeStatus(
        val coRecarga: String,
        val nuMsisdn: String,
        val nuValor: Double? = null,
        val nuPlano: String,
        val stRecarga: String,
        val dsTransacao: String,
        val dsMetodo: String,
        val nuNsu: String
    )

    @Serializable
    data class Transaction(val localTransactionId: String, val localTransactionDate: String)
}

internal object ConsultRechargeStatusEndpoint : Endpoint {
    override val path = "spec-mobile/v1/recarga/consultar-recarga"
    override val method = HttpMethod.Get
}

suspend fun SurfApiKit.consultRechargeStatus(request: ConsultRechargeStatusRequest): ConsultRechargeStatusSuccess =
    client.send(ConsultRechargeStatusEndpoint, query = mapOf("coRecarga" to request.coRecarga))
