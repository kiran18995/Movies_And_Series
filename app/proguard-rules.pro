# Add project specific ProGuard rules here.

# Retrofit / Gson / Coroutines rules
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class retrofit2.** { *; }

# Keep all app classes to prevent Koin/Serialization crashes in release builds
-keep class com.kiran.movie.** { *; }
-keep interface com.kiran.movie.** { *; }
-keep enum com.kiran.movie.** { *; }

# Keep Ktor and Koin to prevent runtime crashes
-keep class io.ktor.** { *; }
-keep class org.koin.** { *; }

# Preserve line numbers for debugging crash logs, but hide original source file name
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# SLF4J rules
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Ktor rules (ignore missing Java management APIs not present on Android)
-dontwarn java.lang.management.**