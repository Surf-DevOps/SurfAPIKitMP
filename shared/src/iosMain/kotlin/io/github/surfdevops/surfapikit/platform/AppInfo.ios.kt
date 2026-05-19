package io.github.surfdevops.surfapikit.platform

import platform.Foundation.NSBundle

actual object AppInfo {
    actual val appVersion: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
    actual val platform: String = "ios"
}
