package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/**
 * La redirection de la base via VETNUTRI_TEST_DATABASE_DIR n'est honorée que hors production :
 * soit sous les tâches de test Gradle (propriété système `vetnutri.testMode=true`, posée dans
 * build.gradle.kts), soit si l'utilisateur confirme explicitement avec
 * VETNUTRI_ALLOW_DATABASE_DIR_OVERRIDE=1 (scripts/run_fresh_desktop.sh). Sinon une variable
 * d'environnement résiduelle ne doit pas pouvoir détourner la base patients d'un poste réel.
 */
private fun databaseDirOverride(): String? {
    val requested = System.getenv("VETNUTRI_TEST_DATABASE_DIR")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null
    val testMode = System.getProperty("vetnutri.testMode") == "true"
    val explicitlyAllowed = System.getenv("VETNUTRI_ALLOW_DATABASE_DIR_OVERRIDE")?.trim() == "1"
    if (!testMode && !explicitlyAllowed) {
        System.err.println(
            "VetNutriMP : VETNUTRI_TEST_DATABASE_DIR ignorée (ni vetnutri.testMode=true, " +
                "ni VETNUTRI_ALLOW_DATABASE_DIR_OVERRIDE=1) — base utilisateur habituelle conservée."
        )
        return null
    }
    System.err.println("VetNutriMP : base de données redirigée vers $requested (mode test / override explicite).")
    return requested
}

fun getDatabasePath(): String {
    val testDatabaseDir = databaseDirOverride()
    val dataDir = if (testDatabaseDir != null) {
        File(testDatabaseDir)
    } else {
        val userHome = System.getProperty("user.home")
        File(userHome, ".vetnutri_mp/data")
    }
    if (!dataDir.exists()) dataDir.mkdirs()
    return File(dataDir, AppDatabase.DATABASE_NAME).absolutePath
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(name = getDatabasePath())
}
