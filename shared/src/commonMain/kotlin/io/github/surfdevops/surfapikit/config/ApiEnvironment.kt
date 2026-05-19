package io.github.surfdevops.surfapikit.config

data class ApiEnvironment(val baseUrl: String) {
    companion object {
        val STAGING = ApiEnvironment("https://plataforma.stage.surf.com.br/api/")
        val PRODUCTION = ApiEnvironment("https://plataforma.surfgroup.com.br/api/")
    }
}
