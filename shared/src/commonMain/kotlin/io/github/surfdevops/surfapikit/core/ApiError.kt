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
    data class Transport(val cause: Throwable) : ApiError() {
        override val message: String get() = cause.message ?: "Network error"
    }

    data object InvalidUrl : ApiError() {
        private fun readResolve(): Any = InvalidUrl
        override val message: String get() = "Invalid URL"
    }

    data class InvalidRequest(override val message: String) : ApiError()

    data class Decoding(val cause: Throwable) : ApiError() {
        override val message: String get() = "Decoding error: ${cause.message}"
    }

    data class Server(val status: Int, val code: Int? = null, val serverMessage: String? = null) : ApiError() {
        override val message: String get() = buildString {
            append("Server error (HTTP $status)")
            if (code != null) append(" - Code: $code")
            if (serverMessage != null) append(" - $serverMessage")
        }
    }

    data class Api(val code: Int, val apiMessage: String) : ApiError() {
        override val message: String get() = apiMessage
    }

    data object Unknown : ApiError() {
        private fun readResolve(): Any = Unknown
        override val message: String get() = "Ocorreu um erro inesperado. Por favor, tente novamente."
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
