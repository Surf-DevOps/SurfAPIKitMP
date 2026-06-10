package io.github.surfdevops.surfapikit.config

data class ApiEnvironment(val baseUrl: String) {
    companion object {
        val STAGING = ApiEnvironment("https://plataforma.stage.surf.com.br/api/")
        val PRODUCTION = ApiEnvironment("https://plataforma.surfgroup.com.br/api/")

        /**
         * Resolve an environment from a string name, mirroring iOS's
         * `APIEnvironment.configure(environmentName:)` mapping.
         * staging/stage/debug -> STAGING; production/prod/release -> PRODUCTION;
         * any other value defaults to PRODUCTION.
         */
        fun from(environmentName: String): ApiEnvironment =
            when (environmentName.lowercase()) {
                "staging", "stage", "debug" -> STAGING
                "production", "prod", "release" -> PRODUCTION
                else -> PRODUCTION
            }
    }
}
