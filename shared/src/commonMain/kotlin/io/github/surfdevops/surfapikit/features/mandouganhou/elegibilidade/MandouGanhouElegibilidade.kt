package io.github.surfdevops.surfapikit.features.mandouganhou.elegibilidade

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class MandouGanhouElegibilidadeSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: Resultado? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Resultado(
        val mvno: String? = null,
        val podeResgatar: Boolean? = null,
        val saldos: Saldos? = null,
        val motivoIndisponibilidade: String? = null
    )

    @Serializable
    data class Saldos(
        @SerialName("LOTERICA_NSU") val lotericaNsu: Saldo? = null,
        @SerialName("CORREIOS_RASTREIO_PAC") val correiosRastreioPac: Saldo? = null,
        @SerialName("CORREIOS_RASTREIO_SEDEX") val correiosRastreioSedex: Saldo? = null
    )

    @Serializable
    data class Saldo(
        val jaBonificadoMb: Int? = null,
        val limiteMb: Int? = null,
        val restanteMb: Int? = null
    )

    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object MandouGanhouElegibilidadeEndpoint : Endpoint {
    override val path = "spec-mobile/v1/mandou-ganhou/elegibilidade"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.mandouGanhouElegibilidade(): MandouGanhouElegibilidadeSuccess =
    client.send(MandouGanhouElegibilidadeEndpoint)
