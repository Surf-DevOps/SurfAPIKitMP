package io.github.surfdevops.surfapikit.features.payments.paymentstatus

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.await
import io.github.surfdevops.surfapikit.core.joinUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Serializable
data class PaymentStatusUpdate(val status: String, val paymentId: String) {
    val isCompleted: Boolean get() = status.lowercase().contains("completed")
    val isFailed: Boolean get() = status.lowercase() in listOf("failed", "rejected", "cancelled", "error")
    val isPending: Boolean get() = listOf("pending", "processing", "awaiting").any { status.lowercase().contains(it) }
}

@Serializable
data class PaymentStatusTimeout(val message: String, val type: String)

@Serializable
data class PaymentStatusHeartbeat(val type: String)

@Serializable
data class PaymentStatusError(val erro: Int, val descricao: String, val transacao: String? = null) : Throwable() {
    override val message: String get() = descricao
}

sealed class PaymentStatusStreamResult {
    data class Update(val update: PaymentStatusUpdate) : PaymentStatusStreamResult()
    data class Timeout(val timeout: PaymentStatusTimeout) : PaymentStatusStreamResult()
    data class Heartbeat(val heartbeat: PaymentStatusHeartbeat) : PaymentStatusStreamResult()
    data class Error(val error: PaymentStatusError) : PaymentStatusStreamResult()
    data object ConnectionClosed : PaymentStatusStreamResult()
}

internal data class PaymentStatusEndpoint(val paymentId: String) : Endpoint {
    override val path: String = "spec-mobile/v1/payments/$paymentId/status-stream"
    override val method = HttpMethod.Get
    override val headers: Map<String, String> = mapOf(
        "Accept" to "text/event-stream",
        "Cache-Control" to "no-cache"
    )
}

fun SurfApiKit.statusStream(paymentId: String): Flow<PaymentStatusStreamResult> = flow {
    val endpoint = PaymentStatusEndpoint(paymentId)
    val fullUrl = joinUrl(client.environment.baseUrl, endpoint.path)
    val json = Json { ignoreUnknownKeys = true }

    val builder = Request.Builder().url(fullUrl).get()
    endpoint.headers.forEach { (k, v) -> builder.header(k, v) }
    builder.header("Connection", "keep-alive")
    // Uses the bare client (no auth interceptor), same as the iOS stream, so inject the
    // token manually here.
    val accessToken = client.tokenStore.accessToken
    if (!accessToken.isNullOrEmpty()) {
        val tokenType = client.tokenStore.tokenType?.takeIf { it.isNotEmpty() } ?: "Bearer"
        builder.header("Authorization", "$tokenType $accessToken")
    }

    // A long-lived event stream must not be killed by the shared read timeout; disable it.
    val streamClient = client.rawClient.newBuilder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()
    val response = streamClient.newCall(builder.build()).await()
    response.use { resp ->
        if (resp.code >= 400) throw ApiError.Server(resp.code)
        val source = resp.body?.source() ?: throw ApiError.InvalidRequest("Empty stream")

        var hasReceivedData = false
        val eventBuffer = StringBuilder()
        while (true) {
            val line = source.readUtf8Line() ?: break
            hasReceivedData = true
            if (line.isEmpty()) {
                val event = eventBuffer.toString()
                eventBuffer.clear()
                if (event.isNotEmpty()) {
                    parseSSEEvent(event, json)?.let { emit(it) }
                }
            } else {
                eventBuffer.appendLine(line)
            }
        }
        if (eventBuffer.isNotEmpty()) {
            parseSSEEvent(eventBuffer.toString(), json)?.let { emit(it) }
        }

        if (!hasReceivedData) throw ApiError.InvalidRequest("Stream finished without receiving any data")
    }
}.flowOn(Dispatchers.IO)

private fun parseSSEEvent(event: String, json: Json): PaymentStatusStreamResult? {
    var eventType: String? = null
    var data: String? = null
    event.split("\n").forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("event:") -> eventType = trimmed.removePrefix("event:").trim()
            trimmed.startsWith("data:") -> data = trimmed.removePrefix("data:").trim()
        }
    }
    val payload = data?.takeIf { it.isNotEmpty() } ?: return null
    if (eventType == "error") {
        return PaymentStatusStreamResult.Error(PaymentStatusError(-1, payload))
    }
    return decodeSSE(payload, json)
}

private fun decodeSSE(payload: String, json: Json): PaymentStatusStreamResult? {
    runCatching { json.decodeFromString<PaymentStatusHeartbeat>(payload) }.getOrNull()?.let {
        if (it.type == "heartbeat") return PaymentStatusStreamResult.Heartbeat(it)
    }
    runCatching { json.decodeFromString<PaymentStatusTimeout>(payload) }.getOrNull()?.let {
        return PaymentStatusStreamResult.Timeout(it)
    }
    runCatching { json.decodeFromString<PaymentStatusUpdate>(payload) }.getOrNull()?.let {
        return PaymentStatusStreamResult.Update(it)
    }
    runCatching { json.decodeFromString<PaymentStatusError>(payload) }.getOrNull()?.let {
        return PaymentStatusStreamResult.Error(it)
    }
    runCatching { (json.parseToJsonElement(payload) as? JsonObject) }.getOrNull()?.let { obj ->
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
        if (type == "heartbeat") return PaymentStatusStreamResult.Heartbeat(PaymentStatusHeartbeat("heartbeat"))
        if (type == "timeout") {
            val msg = obj["message"]?.jsonPrimitive?.contentOrNull ?: "Connection timeout"
            return PaymentStatusStreamResult.Timeout(PaymentStatusTimeout(msg, "timeout"))
        }
        val status = obj["status"]?.jsonPrimitive?.contentOrNull
        val paymentId = obj["paymentId"]?.jsonPrimitive?.contentOrNull
        if (status != null && paymentId != null) {
            return PaymentStatusStreamResult.Update(PaymentStatusUpdate(status, paymentId))
        }
    }
    return PaymentStatusStreamResult.Error(PaymentStatusError(-1, payload))
}
