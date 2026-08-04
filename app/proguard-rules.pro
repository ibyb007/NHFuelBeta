# Firebase & Firestore Rules
-keep class com.google.firebase.** { *; }
-keep class com.google.firestore.** { *; }

# Google Play Services & Identity / Credential Manager
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep class androidx.credentials.** { *; }

# ML Kit Barcode Scanner & CameraX
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-keep class androidx.camera.** { *; }

# Room Entities & Data Converters
-keep class com.nh.fuel.data.** { *; }
# Preserve generic signatures for Gson TypeToken deserialization
-keepattributes Signature, InnerClasses, EnclosingMethod

# --- Gson (official rules, required so R8 doesn't strip TypeToken's
# generic type parameter on shrink/obfuscation passes) ---
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# Keep Data Models used by Gson (fields must survive with original names/types)
-keep class com.nh.fuel.data.** { *; }
-keep class com.nh.fuel.ui.FullStationBackupData { *; }
