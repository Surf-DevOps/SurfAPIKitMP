package io.github.surfdevops.surfapikit.features.authentication.login

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class LoginRequest(
    val nuDocumento: String,
    val dsPassword: String,
    val coMvno: Int
)

/**
 * Resposta do `POST spec-mobile/v2/customer/login` (`CustomerService.loginV2` no
 * spec-mobile). O endpoint responde **HTTP 200 em dois formatos diferentes**:
 *
 * 1. Credenciais validadas (`sucesso = 0`) — `resultado` traz `nuDocumento`,
 *    `registros`, `selectionToken` e `tokenType`.
 * 2. Documento não cadastrado (`sucesso = 1`, `mensagem = "O documento não se
 *    encontra na base"`) — `resultado` traz **somente** `documentoExiste = false`.
 *    O backend faz essa checagem prévia (`documentoExisteNaBase`) antes de validar
 *    a senha, para diferenciar "documento não cadastrado" de "senha inválida".
 *
 * Como os dois vêm com HTTP 200, o [io.github.surfdevops.surfapikit.core.ApiClient]
 * não os trata como erro — por isso os campos do formato (1) são nulos no formato
 * (2). Use [documentNotRegistered] para distinguir.
 */
@Serializable
data class LoginSuccess(
    val sucesso: Int,
    val mensagem: String,
    val resultado: Resultado
) {
    @Serializable
    data class Resultado(
        val nuDocumento: String? = null,
        val registros: List<Registro>? = null,
        val selectionToken: String? = null,
        val tokenType: String? = null,
        /** Só vem no formato "documento não cadastrado", sempre como `false`. */
        val documentoExiste: Boolean? = null
    )

    @Serializable
    data class Registro(
        val coMsisdn: String,
        val dsNome: String,
        val nuMsisdn: String,
        val coMvno: Int
    )

    /**
     * `true` quando a API respondeu sucesso HTTP informando que o documento não está
     * na base — o app deve oferecer o cadastro em vez de seguir para a seleção de
     * linha. Nesse caso [Resultado.registros] e [Resultado.selectionToken] são nulos.
     */
    val documentNotRegistered: Boolean
        get() = resultado.documentoExiste == false
}

internal object LoginEndpoint : Endpoint {
    override val path = "spec-mobile/v2/customer/login"
    override val method = HttpMethod.Post
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.login(request: LoginRequest): LoginSuccess {
    val response: LoginSuccess = client.send(LoginEndpoint, body = request)
    // The native iOS SDK stores the selectionToken in tokenStore.accessToken so the auth
    // header on the subsequent selectLine call uses it as Bearer. We mirror that behavior.
    // Na resposta "documento não cadastrado" não existe selectionToken — não tocar no
    // store nesse caso, para não invalidar uma sessão existente.
    val selectionToken = response.resultado.selectionToken
    if (!selectionToken.isNullOrEmpty()) {
        tokenStore.accessToken = selectionToken
        tokenStore.selectionToken = selectionToken
        tokenStore.tokenType = response.resultado.tokenType
    }
    return response
}
