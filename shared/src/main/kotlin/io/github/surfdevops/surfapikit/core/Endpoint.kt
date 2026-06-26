package io.github.surfdevops.surfapikit.core

interface Endpoint {
    val path: String
    val method: HttpMethod
    val headers: Map<String, String>
        get() = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json"
        )
}
