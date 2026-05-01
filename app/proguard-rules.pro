# Add project specific ProGuard rules here.

# Retrofit / Gson / Coroutines rules
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class retrofit2.** { *; }

# Keep data models used by Gson to prevent parsing crashes after obfuscation
-keep class com.kiran.movie.data.models.** { *; }

# Keep API interfaces used by Retrofit
-keep class com.kiran.movie.api.** { *; }

# Keep Retrofit interface methods safely
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Preserve line numbers for debugging crash logs, but hide original source file name
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile