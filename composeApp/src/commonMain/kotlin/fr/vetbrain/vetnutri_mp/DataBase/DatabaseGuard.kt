package fr.vetbrain.vetnutri_mp.DataBase

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
