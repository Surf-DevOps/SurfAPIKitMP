-keep class io.github.surfdevops.surfapikit.** { *; }
-keepclassmembers class io.github.surfdevops.surfapikit.** {
    *;
}

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class io.github.surfdevops.surfapikit.** {
    kotlinx.serialization.KSerializer serializer(...);
}
