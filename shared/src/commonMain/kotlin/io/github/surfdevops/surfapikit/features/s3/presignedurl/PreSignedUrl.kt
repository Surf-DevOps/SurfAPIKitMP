package io.github.surfdevops.surfapikit.features.s3.presignedurl

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.Endpoint
import io.ktor.http.HttpMethod
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
    val resultado: DownloadUrls? = null
) {
    @Serializable
    data class DownloadUrls(val urls: List<PresignedUrl>)

    @Serializable
    data class PresignedUrl(val id: Int, val url: String, val value: String)
}

internal object PreSignedUploadEndpoint : Endpoint {
    override val path = "spec-mobile/v1/uploads/s3/gerar-presigned-upload"
    override val method = HttpMethod.Get
}

internal object PreSignedDownloadListEndpoint : Endpoint {
    override val path = "spec-mobile/v1/uploads/s3/gerar-presigned-get-list"
    override val method = HttpMethod.Get
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPresignedUploadUrl(request: PreSignedUploadRequest): PreSignedUploadSuccess =
    client.send(PreSignedUploadEndpoint, query = mapOf("key" to request.key))

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPresignedDownloadList(
    mvno: String,
    number: Int? = null,
    stories: Int? = null,
    cpf: Int? = null
): PreSignedDownloadListSuccess {
    val values = resolveBannerOrStoryValues(number, stories, cpf)
    return client.send(
        PreSignedDownloadListEndpoint,
        query = mapOf("key" to "mvno/$mvno/banners/android", "values" to values.joinToString(","))
    )
}

@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getPresignedDownloadNewList(
    mvno: String,
    number: Int? = null,
    stories: Int? = null,
    cpf: Int? = null
): PreSignedDownloadListSuccess {
    val values = resolveBannerOrStoryValues(number, stories, cpf)
    return client.send(
        PreSignedDownloadListEndpoint,
        query = mapOf("key" to "mvno/$mvno/banners", "values" to values.joinToString(","))
    )
}

fun resolveBannerOrStoryValues(number: Int?, stories: Int?, cpf: Int?): List<String> {
    if (number != null && number > 0) return (1..number).map { "banner_$it.png" }
    if (stories != null && stories > 0) return (1..stories).map { "story_$it.png" }
    return if (cpf != null) listOf("$cpf") else emptyList()
}
