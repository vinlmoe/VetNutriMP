# Room ORM — conserver les entités et les implémentations générées par KSP
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# kotlinx.serialization — conserver les classes annotées @Serializable
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class fr.vetbrain.vetnutri_mp.**$$serializer { *; }
-keepclassmembers class fr.vetbrain.vetnutri_mp.** {
    *** Companion;
}
-keepclasseswithmembers class fr.vetbrain.vetnutri_mp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor — sérialisation HTTP
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Multiplatform
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

# Conserver les enums (utilisés pour le mapping nutriments)
-keepclassmembers enum fr.vetbrain.vetnutri_mp.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
}

# Okio
-dontwarn okio.**
-keep class okio.** { *; }

# SQLite bundled
-keep class androidx.sqlite.** { *; }
-dontwarn androidx.sqlite.**

# Conserver les classes de modèles de données
-keep class fr.vetbrain.vetnutri_mp.Data.** { *; }
-keep class fr.vetbrain.vetnutri_mp.DataBase.** { *; }
-keep class fr.vetbrain.vetnutri_mp.Enumer.** { *; }

# Compose
-dontwarn androidx.compose.**
