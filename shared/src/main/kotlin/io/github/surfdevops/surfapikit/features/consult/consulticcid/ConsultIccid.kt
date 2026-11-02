package io.github.surfdevops.surfapikit.features.consult.consulticcid

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class ConsultIccidRequest(val nuIccid: String)

/** Flags `st*` chegam como número ou como `"SIM"`/`"NAO"`; normaliza para 1/0. */
internal object FlexibleFlagSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val primitive = element as? JsonPrimitive ?: return element
        if (!primitive.isString) return element
        return when (primitive.content.trim().uppercase()) {
            "SIM" -> JsonPrimitive(1)
            "NAO", "NÃO" -> JsonPrimitive(0)
            else -> element
        }
    }
}

/** O chip `NOT USED` responde com `transacao` e sem `mensagem`; aceitos aqui. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ConsultIccidSuccess(
    val sucesso: Int,
    @JsonNames("transacao") val nuTransacao: String,
    val mensagem: String = "",
    val resultado: LineDetail? = null,
    val transaction: TransactionInfo? = null
) {
    /**
     * O backend também responde com nomes sem prefixo húngaro
     * (ex.: `iccid`, `msisdn`, `status`); aceitos via [JsonNames].
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    data class LineDetail(
        val coMsisdn: Long? = null,
        @Serializable(with = FlexibleFlagSerializer::class) val stPortin: Int? = null,
        val vlSaldo: Double? = null,
        val dtAtivacao: String? = null,
        @JsonNames("iccid") val nuIccid: String? = null,
        @JsonNames("imsi") val nuImsi: Long? = null,
        val dtPortin: String? = null,
        val dtPortout: String? = null,
        @JsonNames("documento") val nuDocumento: String? = null,
        val dsObservacao: String? = null,
        @JsonNames("status") val noMsisdnStatus: String? = null,
        @JsonNames("mvno") val noMvno: String? = null,
        @JsonNames("submvno") val noSubmvno: String? = null,
        @JsonNames("msisdn") val nuMsisdn: Long? = null,
        val noPlano: String? = null,
        val qtDadoRestante: Double? = null,
        val qtMinutoRestante: Double? = null,
        val dtValidade: String? = null,
        val dtPlanoExpira: String? = null,
        val stBloqueioVozOriginada: Int? = null,
        val stBloqueioSmsOriginado: Int? = null,
        val stBloqueioDado: Int? = null,
        val dtUltimaRecarga: String? = null,
        val coMvno: Int? = null,
        val coSubmvno: Int? = null,
        val coPlano: Int? = null,
        val stBloqueioConsumo: Int? = null,
        @Serializable(with = FlexibleFlagSerializer::class) val stBloqueioChip: Int? = null,
        val qtSmsRestante: Double? = null,
        val stBloqueioVozTerminada: Int? = null,
        val dtBloqueioVozTerminada: String? = null,
        val stInadimplencia: Int? = null,
        val noUsuario: String? = null,
        val stBlackFriday: Int? = null,
        val nuPin1: String? = null,
        val nuPuk1: String? = null,
        val nuPin2: String? = null,
        val nuPuk2: String? = null,
        val qtConsumoDado: Double? = null,
        val qtConsumoVoz: Double? = null,
        val qtConsumoSMS: Double? = null,
        val stMsisdnGold: Int? = null,
        val nuPlano: String? = null,
        val accountId: Long? = null,
        val stPlanoControle: String? = null,
        val tipoDocumento: String? = null
    )

    @Serializable
    data class TransactionInfo(
        val globalTransactionId: String? = null,
        val localTransactionId: String? = null,
        val localTransactionDate: String? = null
    )
}

internal object ConsultIccidEndpoint : Endpoint {
    override val path = "spec-mobile/v2/consumer/consulta-iccid"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.consultIccid(request: ConsultIccidRequest): ConsultIccidSuccess =
    client.send(ConsultIccidEndpoint, query = mapOf("nuIccid" to request.nuIccid))
