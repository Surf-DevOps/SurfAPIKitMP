package io.github.surfdevops.surfapikit.platform

object AppInfo {
    val appVersion: String
        get() = runCatching {
            val ctx = AppContextHolder.context
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0.0"
        }.getOrElse { "1.0.0" }

    val platform: String = "android"
}
