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
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Data Models
-keep class com.nh.fuel.data.** { *; }
-keep class com.nh.fuel.ui.FullStationBackupData { *; }
