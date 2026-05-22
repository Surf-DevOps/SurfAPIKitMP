package io.github.surfdevops.surfapikit.features.authentication.validateuserlogged

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class ValidateUserLoggedRequest(val nuMsisdn: String)

@Serializable
data class ValidateUserLoggedSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val coMsisdn: Long,
        val nuMsisdn: Long,
        val noMvno: String,
        val nuDocumento: String,
        @SerialName("detalhe_consumo") val detalheConsumo: DetalheConsumo,
        @SerialName("detalhe_plano") val detalhePlano: DetalhePlano
    )

    @Serializable
    data class DetalheConsumo(
        val qtVozAtribuido: Int,
        val consumoVoz: Int,
        val qtDadoAtribuido: Int,
        val consumoDado: Int,
        val qtSmsAtribuido: Int,
        val consumoSms: Int,
        val dtPlano: String,
        val dtExpiracao: String
    )

    @Serializable
    data class DetalhePlano(
        val noPlano: String,
        val nuPlano: String,
        val valor: Int,
        val diasValidade: Int,
        val qtDado: Int,
        val qtVoz: Int,
        val qtSms: Int,
        val dadoPortabilidade: Int,
        val vozPortabilidade: Int,
        val smsPortabilidade: Int,
        val planoRecorrencia: String,
        val dadoRecorrencia: Int,
        val vozRecorrencia: Int,
        val smsRecorrencia: Int
    )
}

internal object ValidateUserLoggedEndpoint : Endpoint {
    override val path = "spec-mobile/v2/auth/validate/40199907"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.validateUserLogged(request: ValidateUserLoggedRequest): ValidateUserLoggedSuccess =
    client.send(ValidateUserLoggedEndpoint, query = mapOf("nuMsisdn" to request.nuMsisdn))
