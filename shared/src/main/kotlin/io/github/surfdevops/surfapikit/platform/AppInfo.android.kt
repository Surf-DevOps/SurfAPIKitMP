package io.github.surfdevops.surfapikit.platform

object AppInfo {
    private val semVerPrefix = Regex("""^\d+\.\d+\.\d+""")

    /**
     * Sempre no formato X.Y.Z: builds debug têm versionNameSuffix (ex. "1.1.0-debug"),
     * mas o backend valida esse valor com regex estrito de SemVer.
     */
    val appVersion: String
        get() = runCatching {
            val ctx = AppContextHolder.context
            val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0.0"
            semVerPrefix.find(versionName)?.value ?: versionName
        }.getOrElse { "1.0.0" }

    val platform: String = "android"
}
