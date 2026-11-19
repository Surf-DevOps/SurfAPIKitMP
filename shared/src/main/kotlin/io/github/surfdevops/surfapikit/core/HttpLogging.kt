package io.github.surfdevops.surfapikit.core

import okhttp3.logging.HttpLoggingInterceptor

/**
 * Nível do log HTTP do SDK. O padrão é [NONE]: o SDK roda dentro do app de produção e
 * não sabe se o build é debug, então quem liga o log é o app (ex.: `BODY` só em debug).
 */
enum class HttpLogLevel {
    NONE,
    BASIC,
    HEADERS,
    BODY,
}

/**
 * Mascara dados sensíveis antes de qualquer linha do log HTTP ir para o logcat:
 * cartão, CVV, senha, PIN/PUK e tokens, em JSON (`"campo": valor`) e em form
 * (`campo=valor`). A comparação do nome do campo ignora maiúsculas.
 */
object SensitiveDataRedactor {

    const val MASK = "***"

    val SENSITIVE_KEYS: Set<String> = setOf(
        // cartão
        "cardNumber", "number", "cvv", "cvc", "securityCode", "holderName",
        "cardHolderName", "expiration", "expiryMonth", "expiryYear", "expirationDate",
        // senha e PIN/PUK
        "password", "dsPassword", "senha", "newPassword", "currentPassword", "oldPassword",
        "pin", "puk", "nuPin1", "nuPin2", "nuPuk1", "nuPuk2",
        // tokens
        "token", "accessToken", "refreshToken", "selectionToken", "idToken",
    )

    /** Headers que o interceptor troca por "██". */
    val SENSITIVE_HEADERS: Set<String> = setOf("Authorization", "token", "Cookie", "Set-Cookie")

    private val keysPattern = SENSITIVE_KEYS.joinToString("|") { Regex.escape(it) }

    // "campo": "texto" | número | true/false
    private val jsonField = Regex(
        "(\"(?:$keysPattern)\"\\s*:\\s*)(\"(?:[^\"\\\\]|\\\\.)*\"|-?\\d+(?:\\.\\d+)?|true|false)",
        RegexOption.IGNORE_CASE,
    )

    // campo=valor em form-urlencoded ou query string
    private val formField = Regex(
        "((?:^|[?&])(?:$keysPattern)=)[^&\\s]*",
        RegexOption.IGNORE_CASE,
    )

    fun redact(line: String): String =
        line
            .replace(jsonField) { "${it.groupValues[1]}\"$MASK\"" }
            .replace(formField) { "${it.groupValues[1]}$MASK" }

    internal fun interceptor(level: HttpLogLevel): HttpLoggingInterceptor? {
        val okLevel = when (level) {
            HttpLogLevel.NONE -> return null
            HttpLogLevel.BASIC -> HttpLoggingInterceptor.Level.BASIC
            HttpLogLevel.HEADERS -> HttpLoggingInterceptor.Level.HEADERS
            HttpLogLevel.BODY -> HttpLoggingInterceptor.Level.BODY
        }
        return HttpLoggingInterceptor { message ->
            HttpLoggingInterceptor.Logger.DEFAULT.log(redact(message))
        }.apply {
            this.level = okLevel
            SENSITIVE_HEADERS.forEach(::redactHeader)
        }
    }
}
