package io.github.surfdevops.surfapikit.features.viacep

import io.github.surfdevops.surfapikit.core.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class ViaCEPResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val ibge: String? = null,
    val gia: String? = null,
    val ddd: String? = null,
    val siafi: String? = null,
    val erro: Boolean? = null
)

data class ViaCEPRequest(val cep: String)

sealed class ViaCEPError : Throwable() {
    data object InvalidCEP : ViaCEPError() { override val message = "CEP inválido. O CEP deve conter exatamente 8 dígitos." }
    data object InvalidURL : ViaCEPError() { override val message = "URL inválida." }
    data object NetworkError : ViaCEPError() { override val message = "Erro de conexão com o servidor." }
    data object DecodingError : ViaCEPError() { override val message = "Erro ao processar a resposta do servidor." }
    data object CepNotFound : ViaCEPError() { override val message = "CEP não encontrado." }
}

object ViaCEPClient {
    private const val BASE_URL = "https://viacep.com.br/ws/"
    private val http = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun buscarCEP(cep: String): ViaCEPResponse {
        val cleanCEP = cep.filter { it.isDigit() }
        if (cleanCEP.length != 8) throw ViaCEPError.InvalidCEP

        val request = Request.Builder().url("$BASE_URL$cleanCEP/json/").get().build()
        val response = try {
            http.newCall(request).await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Throwable) {
            throw ViaCEPError.NetworkError
        }
        return response.use { resp ->
            if (resp.code !in 200..299) throw ViaCEPError.NetworkError
            val text = resp.body?.string() ?: throw ViaCEPError.DecodingError
            val body = try {
                json.decodeFromString(ViaCEPResponse.serializer(), text)
            } catch (_: Throwable) {
                throw ViaCEPError.DecodingError
            }
            if (body.erro == true) throw ViaCEPError.CepNotFound
            body
        }
    }
}
