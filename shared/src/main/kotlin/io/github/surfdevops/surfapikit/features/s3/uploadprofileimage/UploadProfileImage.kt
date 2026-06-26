package io.github.surfdevops.surfapikit.features.s3.uploadprofileimage

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.core.await
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.cancellation.CancellationException

data class UploadProfileImageRequest(val key: String)

@Serializable
data class UploadProfileImageResponse(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: PresignedUrl? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class PresignedUrl(val url: String)

    @Serializable
    data class Transaction(val localTransactionId: String, val localTransactionDate: String)
}

internal object UploadProfileImageEndpoint : Endpoint {
    override val path = "spec-mobile/v1/uploads/s3/gerar-presigned-upload"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uploadProfileImage(request: UploadProfileImageRequest): UploadProfileImageResponse =
    client.send(UploadProfileImageEndpoint, query = mapOf("key" to request.key))

private val rawHttp = OkHttpClient()

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uploadImageToS3(url: String, imageData: ByteArray) {
    try {
        val request = Request.Builder()
            .url(url)
            .put(imageData.toRequestBody())
            .build()
        val response = rawHttp.newCall(request).await()
        response.use { if (it.code >= 400) throw ApiError.Server(it.code) }
    } catch (e: ApiError) {
        throw e
    } catch (e: Throwable) {
        throw ApiError.Transport(e)
    }
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.uploadProfileImageComplete(key: String, imageData: ByteArray) {
    val response = uploadProfileImage(UploadProfileImageRequest(key))
    val urlString = response.resultado?.url ?: throw ApiError.Unknown
    uploadImageToS3(urlString, imageData)
}
