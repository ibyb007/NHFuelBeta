# Firebase & ML Kit ProGuard Rules
-keep class com.google.firebase.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Room entities and Converters
-keep class com.nh.fuel.data.** { *; }
