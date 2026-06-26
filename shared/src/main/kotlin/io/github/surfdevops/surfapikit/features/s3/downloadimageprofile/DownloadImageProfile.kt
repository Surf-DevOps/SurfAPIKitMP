package io.github.surfdevops.surfapikit.features.s3.downloadimageprofile

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.await
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
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
    override val path = "spec-mobile/v2/uploads/s3/gerar-presigned-get-list"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.downloadImageProfile(request: DownloadImageProfileRequest): DownloadImageProfileResponse =
    client.send(DownloadImageProfileEndpoint, query = mapOf("key" to request.key, "values" to request.values))

private val rawHttp = OkHttpClient()

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.downloadImageFromS3(url: String): ByteArray = try {
    val request = Request.Builder().url(url).get().build()
    val response = rawHttp.newCall(request).await()
    response.use { resp ->
        if (resp.code >= 400) throw ApiError.Server(resp.code)
        resp.body?.bytes() ?: ByteArray(0)
    }
} catch (e: ApiError) {
    throw e
} catch (e: Throwable) {
    throw ApiError.Transport(e)
}
