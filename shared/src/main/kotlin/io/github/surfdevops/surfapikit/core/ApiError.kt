package io.github.surfdevops.surfapikit.core

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val erro: Int,
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
            Unknown -> "Ocorreu um erro inesperado. Por favor, tente novamente."
        }

    val errorCode: Int?
        get() = when (this) {
            is Server -> status
            is Api -> code
            else -> null
        }

    val userDisplayMessage: String
        get() = when (this) {
            is Api -> "Erro $code\n$apiMessage"
            is Server -> if (code != null && serverMessage != null) "Erro $code\n$serverMessage" else message
            else -> message
        }
}
