# Règles R8 de VetNutriMP
#
# Principe : ne conserver que ce que l'application résout par réflexion ou par nom
# (kotlinx.serialization, Room, enums résolus par label). Les bibliothèques (Kotlin,
# coroutines, Ktor, okio, sqlite-bundled, Compose) embarquent leurs propres règles
# « consumer » : les anciens `-keep class <lib>.** { *; }` globaux neutralisaient
# le shrinking et l'obfuscation de tout le runtime sans apporter de garantie
# supplémentaire ; ils sont remplacés par de simples `-dontwarn`.

# Room ORM — conserver les entités et les implémentations générées par KSP
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverters class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract *;
}

# kotlinx.serialization — règles recommandées par la bibliothèque
# (https://github.com/Kotlin/kotlinx.serialization#android)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
# Conserver les classes @Serializable (noms + membres) : les noms de propriétés servent de clés JSON
# dans les exports/imports/sauvegardes et le JSON embarqué (vetnutri_export_init.json).
-keep @kotlinx.serialization.Serializable class fr.vetbrain.vetnutri_mp.** { *; }
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

# Enums de l'application : résolus par nom / label (NutrientResolver, valueOf, sérialisation)
-keepclassmembers enum fr.vetbrain.vetnutri_mp.** { *; }
-keep enum fr.vetbrain.vetnutri_mp.Enumer.** { *; }

# Kotlin / coroutines — les artefacts embarquent leurs règles consumer
-keep class kotlin.Metadata { *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

# Ktor — règles consumer fournies par les artefacts
-dontwarn io.ktor.**

# Okio — règles consumer fournies par l'artefact
-dontwarn okio.**

# SQLite bundled — règles consumer fournies par l'artefact
-dontwarn androidx.sqlite.**

# Compose
-dontwarn androidx.compose.**
