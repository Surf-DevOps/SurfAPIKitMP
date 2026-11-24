package io.github.surfdevops.surfapikit.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * `erro` da API normalmente é um código numérico interno (ex.: `156`), mas
 * alguns fluxos mais novos (ex.: Mandou Ganhou) passaram a usar um código
 * simbólico em string (ex.: `"CODIGO_JA_RESGATADO"`). Decodifica qualquer um
 * dos dois formatos sem precisar de dois campos/classes separadas — espelha
 * `APIErrorCode` do SDK iOS.
 */
@Serializable(with = ApiErrorCodeSerializer::class)
sealed class ApiErrorCode {
    data class Numeric(val value: Int) : ApiErrorCode()
    data class Symbolic(val value: String) : ApiErrorCode()
}

object ApiErrorCodeSerializer : KSerializer<ApiErrorCode> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ApiErrorCode", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ApiErrorCode) {
        when (value) {
            is ApiErrorCode.Numeric -> encoder.encodeInt(value.value)
            is ApiErrorCode.Symbolic -> encoder.encodeString(value.value)
        }
    }

    override fun deserialize(decoder: Decoder): ApiErrorCode {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("ApiErrorCodeSerializer só funciona com kotlinx.serialization.json.Json")
        val primitive = jsonDecoder.decodeJsonElement() as? JsonPrimitive
            ?: error("Esperado um valor primitivo (número ou string) para \"erro\"")
        return primitive.intOrNull?.let { ApiErrorCode.Numeric(it) } ?: ApiErrorCode.Symbolic(primitive.content)
    }
}

@Serializable
data class ApiErrorResponse(
    val erro: ApiErrorCode,
    val transacao: String? = null,
    val descricao: String,
    val transaction: Transaction? = null
) {
    @Serializable
    data class Transaction(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

sealed class ApiError : Throwable() {
    data class Transport(override val cause: Throwable) : ApiError()
    data object InvalidUrl : ApiError()
    data class InvalidRequest(val msg: String) : ApiError()
    data class Decoding(override val cause: Throwable) : ApiError()
    data class Server(val status: Int, val code: Int? = null, val serverMessage: String? = null) : ApiError()
    data class Api(val code: Int, val apiMessage: String) : ApiError()
    /** Mesma forma que [Api], mas pra quando `erro` vem como código simbólico
     *  (string) em vez de numérico — ver [ApiErrorCode]. */
    data class ApiSymbolic(val symbolicCode: String, val apiMessage: String) : ApiError()
    data object Unknown : ApiError()

    override val message: String
        get() = when (this) {
            is Transport -> cause.message ?: "Network error"
            is InvalidUrl -> "Invalid URL"
            is InvalidRequest -> msg
            is Decoding -> "Decoding error: ${cause.message ?: ""}"
            is Server -> buildString {
                append("Server error (HTTP $status)")
                if (code != null) append(" - Code: $code")
                if (serverMessage != null) append(" - $serverMessage")
            }
            is Api -> apiMessage
            is ApiSymbolic -> apiMessage
            Unknown -> "Ocorreu um erro inesperado. Por favor, tente novamente."
        }

    val errorCode: Int?
        get() = when (this) {
            is Server -> status
            is Api -> code
            else -> null
        }

    /** Código simbólico (ex.: `"CODIGO_JA_RESGATADO"`), quando o erro veio
     *  nesse formato. `null` para qualquer outro caso, incluindo [Api] numérico. */
    val symbolicErrorCode: String?
        get() = (this as? ApiSymbolic)?.symbolicCode

    val userDisplayMessage: String
        get() = when (this) {
            is Api -> "Erro $code\n$apiMessage"
            is ApiSymbolic -> "Erro $symbolicCode\n$apiMessage"
            is Server -> if (code != null && serverMessage != null) "Erro $code\n$serverMessage" else message
            // Nunca expor texto de exceção de transporte/parse ao usuário — `message`
            // continua técnico para logs, aqui vai a versão amigável.
            is Transport -> transportUserMessage
            is Decoding -> "Não foi possível processar a resposta do servidor. Tente novamente."
            else -> message
        }

    private val transportUserMessage: String
        get() = when (cause) {
            is UnknownHostException, is ConnectException ->
                "Sem conexão com a internet. Verifique sua rede e tente novamente."
            is SocketTimeoutException ->
                "A conexão demorou mais do que o esperado. Tente novamente."
            is SSLException ->
                "Não foi possível estabelecer uma conexão segura. Tente novamente."
            else ->
                "Não foi possível concluir a operação. Verifique sua conexão e tente novamente."
        }
}
