package io.github.surfdevops.surfapikit.features.viacep

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    private val http = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun buscarCEP(cep: String): ViaCEPResponse {
        val cleanCEP = cep.filter { it.isDigit() }
        if (cleanCEP.length != 8) throw ViaCEPError.InvalidCEP

        val response: HttpResponse = try {
            http.get("$BASE_URL$cleanCEP/json/")
        } catch (_: Throwable) {
            throw ViaCEPError.NetworkError
        }
        if (response.status.value !in 200..299) throw ViaCEPError.NetworkError

        val body: ViaCEPResponse = try {
            response.body()
        } catch (_: Throwable) {
            throw ViaCEPError.DecodingError
        }
        if (body.erro == true) throw ViaCEPError.CepNotFound
        return body
    }
}
