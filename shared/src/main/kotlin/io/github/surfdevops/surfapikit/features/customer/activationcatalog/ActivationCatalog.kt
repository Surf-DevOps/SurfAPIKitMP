package io.github.surfdevops.surfapikit.features.customer.activationcatalog

import io.github.surfdevops.surfapikit.SurfApiKit
import io.github.surfdevops.surfapikit.core.ApiError
import io.github.surfdevops.surfapikit.core.Endpoint
import io.github.surfdevops.surfapikit.core.HttpMethod
import io.github.surfdevops.surfapikit.features.catalog.catalog.CatalogSuccess
import kotlin.coroutines.cancellation.CancellationException

internal object ActivationCatalogEndpoint : Endpoint {
    override val path = "spec-mobile/v4/customer/catalogo-ativacao"
    override val method = HttpMethod.Get
}

/**
 * Planos do app para o chip `NOT USED`, antes do cadastro (sem token). Mesmo formato do
 * [io.github.surfdevops.surfapikit.features.catalog.catalog.getCatalog].
 */
@Throws(ApiError::class, CancellationException::class)
suspend fun SurfApiKit.getActivationCatalog(nuIccid: String, responsive: Boolean = true): CatalogSuccess =
    client.send(
        ActivationCatalogEndpoint,
        query = mapOf("nuIccid" to nuIccid, "responsivo" to responsive.toString()),
    )
