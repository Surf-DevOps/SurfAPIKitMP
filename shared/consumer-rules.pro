-keep class io.github.surfdevops.surfapikit.** { *; }
-keepclassmembers class io.github.surfdevops.surfapikit.** {
    *;
}

# kotlinx.serialization (Signature + EnclosingMethod carry the generic type info the
# reflective serializer(Type) lookup relies on)
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt
# Keep generated serializers explicitly (belt-and-suspenders for the reflective request path)
-keep,includedescriptorclasses class io.github.surfdevops.surfapikit.**$$serializer { *; }
-keepclassmembers class io.github.surfdevops.surfapikit.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.surfdevops.surfapikit.** {
    kotlinx.serialization.KSerializer serializer(...);
}
