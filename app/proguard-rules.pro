# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.soraiyu.foxappmemo.**$$serializer { *; }
-keepclassmembers class com.soraiyu.foxappmemo.** {
    *** Companion;
}
-keepclasseswithmembers class com.soraiyu.foxappmemo.** {
    kotlinx.serialization.KSerializer serializer(...);
}
