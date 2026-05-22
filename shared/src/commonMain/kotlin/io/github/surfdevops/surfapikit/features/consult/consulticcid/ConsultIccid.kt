package io.github.surfdevops.surfapikit.features.consult.consulticcid

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class ConsultIccidRequest(val nuIccid: String)

@Serializable
data class ConsultIccidSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: ActivationData? = null
) {
    @Serializable
    data class ActivationData(
        val msisdn: Long? = null,
        val iccid: String? = null,
        val imsi: Long? = null,
        val coPlano: Int? = null,
        val dtPlanoExpira: String? = null,
        val dtUltimaRecarga: String? = null,
        val dtAtivacao: String? = null,
        val nuPlano: String? = null,
        val noPlano: String? = null,
        val documento: String? = null,
        val status: String,
        val accountId: Int? = null,
        val mvno: String? = null,
        val submvno: String? = null,
        val stPlanoControle: String? = null,
        val stPortin: Int? = null,
        val tipoDocumento: String? = null
    )
}

internal object ConsultIccidEndpoint : Endpoint {
    override val path = "spec-mobile/v1/consumer/consulta-iccid"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.consultIccid(request: ConsultIccidRequest): ConsultIccidSuccess =
    client.send(ConsultIccidEndpoint, query = mapOf("nuIccid" to request.nuIccid))
