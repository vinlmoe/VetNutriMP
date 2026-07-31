package fr.vetbrain.vetnutri_mp.DataBase

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.datetime.Clock
import okio.FileSystem
import okio.Path.Companion.toPath

private val DB_EXTENSIONS = listOf("", "-wal", "-shm")

/**
 * Copie les fichiers DB (+ WAL/SHM) en .bak avant toute migration.
 * Appelé systématiquement au démarrage, avant que Room n'ouvre la base.
 */
fun backupDatabaseFiles(dbPath: String) {
    val fs = FileSystem.SYSTEM
    for (ext in DB_EXTENSIONS) {
        try {
            val src = "$dbPath$ext".toPath()
            if (fs.exists(src)) {
                fs.copy(src, "$dbPath$ext.bak".toPath())
            }
        } catch (_: Exception) {}
    }
}

/**
 * Force une première lecture SQLite avant la construction paresseuse de Room.
 *
 * `RoomDatabase.Builder.build()` n'ouvre pas immédiatement la connexion : sans ce contrôle,
 * une corruption n'est découverte qu'au premier appel DAO, hors du `try/catch` d'initialisation.
 */
fun isDatabaseReadable(dbPath: String): Boolean {
    val path = dbPath.toPath()
    if (!FileSystem.SYSTEM.exists(path)) return true

    return try {
        BundledSQLiteDriver().open(dbPath).use { connection ->
            connection.prepare("PRAGMA schema_version").use { statement ->
                statement.step()
            }
        }
        true
    } catch (_: Exception) {
        false
    }
}

/**
 * Déplace les fichiers DB corrompus vers .corrupt.<epoch> et laisse le chemin principal libre.
 * Room crée ensuite une base vide propre. Le .bak reste disponible pour restauration manuelle.
 */
fun rotateCorruptDatabaseFiles(dbPath: String) {
    val fs = FileSystem.SYSTEM
    val ts = Clock.System.now().epochSeconds
    for (ext in DB_EXTENSIONS) {
        try {
            val src = "$dbPath$ext".toPath()
            if (fs.exists(src)) {
                fs.atomicMove(src, "$dbPath$ext.corrupt.$ts".toPath())
            }
        } catch (_: Exception) {}
    }
}
