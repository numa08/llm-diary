# llm-diary ProGuard rules

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.* { *; }

# LlamaCpp JNI
-keep class net.numa08.llmdiary.llm.LlamaCppEngine {
    native <methods>;
}

# Markwon
-keep class io.noties.markwon.** { *; }
