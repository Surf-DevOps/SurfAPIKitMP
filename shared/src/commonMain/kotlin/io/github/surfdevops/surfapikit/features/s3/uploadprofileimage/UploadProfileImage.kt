package io.github.surfdevops.surfapikit.features.s3.uploadprofileimage

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

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

suspend fun SurfApiKit.uploadProfileImage(request: UploadProfileImageRequest): UploadProfileImageResponse =
    client.send(UploadProfileImageEndpoint, query = mapOf("key" to request.key))

private val rawHttp = HttpClient()

suspend fun SurfApiKit.uploadImageToS3(url: String, imageData: ByteArray) {
    try {
        val response: HttpResponse = rawHttp.put(url) { setBody(imageData) }
        if (response.status.value >= 400) throw ApiError.Server(response.status.value)
    } catch (e: ApiError) {
        throw e
    } catch (e: Throwable) {
        throw ApiError.Transport(e)
    }
}

suspend fun SurfApiKit.uploadProfileImageComplete(key: String, imageData: ByteArray) {
    val response = uploadProfileImage(UploadProfileImageRequest(key))
    val urlString = response.resultado?.url ?: throw ApiError.Unknown
    uploadImageToS3(urlString, imageData)
}
