# Add project specific ProGuard rules here.

# Retrofit / Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep data models used by Gson to prevent parsing crashes after obfuscation
-keep class com.kiran.movie.data.models.** { *; }

# Preserve line numbers for debugging crash logs, but hide original source file name
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile