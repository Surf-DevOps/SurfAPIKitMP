package io.github.surfdevops.surfapikit.core

/**
 * HTTP verbs supported by [Endpoint]. Replaces the former dependency on
 * `io.ktor.http.HttpMethod` so the SDK no longer pulls in Ktor — the member names
 * (`Get`, `Post`, ...) are kept identical so feature endpoints only need to swap the
 * import, not their `override val method = HttpMethod.X` declarations.
 */
enum class HttpMethod {
    Get,
    Post,
    Put,
    Patch,
    Delete,
    Head,
    Options,
}
