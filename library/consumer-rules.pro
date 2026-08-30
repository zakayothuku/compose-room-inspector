# Preserve Room Inspector models and drivers during R8 obfuscation
-keep class io.github.zakayothuku.roominspector.model.** { *; }
-keepclassmembers class io.github.zakayothuku.roominspector.model.** { *; }
-keep class io.github.zakayothuku.roominspector.driver.** { *; }
-keepclassmembers class io.github.zakayothuku.roominspector.driver.** { *; }
-keep class io.github.zakayothuku.roominspector.repository.** { *; }
-keepclassmembers class io.github.zakayothuku.roominspector.repository.** { *; }
