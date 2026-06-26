package io.github.surfdevops.surfapikit.features.s3.presignedurl

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import kotlinx.serialization.Serializable
import io.github.surfdevops.surfapikit.core.ApiError
import kotlin.coroutines.cancellation.CancellationException

data class PreSignedUploadRequest(val key: String)

@Serializable
data class PreSignedUploadSuccess(
    val sucesso: Int,
    val transacao: String,
    val descricao: String,
    val resultado: UploadUrl? = null,
    val transaction: Transaction? = null
) {
    @Serializable
    data class UploadUrl(val url: String)

    @Serializable
    data class Transaction(
        val globalTransactionId: String,
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

@Serializable
data class PreSignedDownloadListSuccess(
    val sucesso: Int,
    val transacao: String? = null,
    val descricao: String? = null,
    val resultado: DownloadUrls? = null,
    val transaction: DownloadTransaction? = null
) {
    @Serializable
    data class DownloadUrls(val urls: List<PresignedUrl>)

    @Serializable
    data class PresignedUrl(
        val id: Int,
        val url: String,
        val value: String,
        val path: String? = null
    )

    @Serializable
    data class DownloadTransaction(
        val localTransactionId: String,
        val localTransactionDate: String
    )
}

internal object PreSignedUploadEndpoint : Endpoint {
    override val path = "spec-mobile/v1/uploads/s3/gerar-presigned-upload"
    override val method = HttpMethod.Get
}

internal object PreSignedDownloadListEndpoint : Endpoint {
    override val path = "spec-mobile/v2/uploads/s3/gerar-presigned-get-list"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPresignedUploadUrl(request: PreSignedUploadRequest): PreSignedUploadSuccess =
    client.send(PreSignedUploadEndpoint, query = mapOf("key" to request.key))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPresignedDownloadList(
    mvno: String,
    values: String = ""
): PreSignedDownloadListSuccess =
    client.send(
        PreSignedDownloadListEndpoint,
        query = mapOf("key" to "mvno/$mvno/banners", "values" to values)
    )
