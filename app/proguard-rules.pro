# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\chakir\AppData\Local\Android\Sdk\tools\proguard\proguard-android-optimize.txt
# while the flags in this file are appended to flags specified in
# C:\Users\chakir\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.DescriptorsKt
-keep,allowobfuscation,allowshrinking class kotlinx.serialization.** { *; }

# Keep data classes generic
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <init>(...);
}

# Keep our Domain and DTO models explicitly to be safe
-keep class com.chakir.aggregatorhubplex.domain.model.** { *; }
-keep class com.chakir.aggregatorhubplex.data.dto.** { *; }
-keep class com.chakir.aggregatorhubplex.data.local.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}