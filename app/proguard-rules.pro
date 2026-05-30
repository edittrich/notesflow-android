# Keep GSON data models from being obfuscated or stripped
-keep class de.edittrich.notesflow.data.model.** { *; }

# Keep GSON class members from being obfuscated/removed
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
