package io.github.surfdevops.surfapikit.features.s3.downloadimageprofile

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

data class DownloadImageProfileRequest(val key: String, val values: String)

@Serializable
data class DownloadImageProfileResponse(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: ResultUrls? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class ResultUrls(val urls: List<ProfileImageUrl>)

    @Serializable
    data class ProfileImageUrl(val id: Int, val url: String, val value: String)

    @Serializable
    data class Transaction(val localTransactionId: String, val localTransactionDate: String)
}

internal object DownloadImageProfileEndpoint : Endpoint {
    override val path = "spec-mobile/v1/uploads/s3/gerar-presigned-get-list"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.downloadImageProfile(request: DownloadImageProfileRequest): DownloadImageProfileResponse =
    client.send(DownloadImageProfileEndpoint, query = mapOf("key" to request.key, "values" to request.values))

private val rawHttp = HttpClient()

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.downloadImageFromS3(url: String): ByteArray = try {
    val response: HttpResponse = rawHttp.get(url)
    if (response.status.value >= 400) throw ApiError.Server(response.status.value)
    response.body()
} catch (e: ApiError) {
    throw e
} catch (e: Throwable) {
    throw ApiError.Transport(e)
}
