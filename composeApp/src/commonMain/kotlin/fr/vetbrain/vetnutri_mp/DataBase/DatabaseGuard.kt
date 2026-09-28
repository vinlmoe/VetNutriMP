package fr.vetbrain.vetnutri_mp.DataBase

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.datetime.Clock
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

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

/**
 * Version de schéma (PRAGMA user_version, écrite par Room) d'un fichier de base existant.
 *
 * @return null si le fichier n'existe pas ou n'est pas lisible
 */
fun readDatabaseVersion(dbPath: String): Int? {
    if (!FileSystem.SYSTEM.exists(dbPath.toPath())) return null
    return try {
        BundledSQLiteDriver().open(dbPath).use { connection ->
            connection.prepare("PRAGMA user_version").use { statement ->
                if (statement.step()) statement.getLong(0).toInt() else null
            }
        }
    } catch (_: Exception) {
        null
    }
}

/** Chemin d'une base mise de côté pour une version de schéma donnée. */
fun parkedDatabasePath(dbPath: String, version: Int): String = "$dbPath.v$version"

/** Déplace les fichiers (+ WAL/SHM) de [from] vers [to], sans jamais écraser une cible existante. */
private fun moveDatabaseFiles(from: String, to: String) {
    val fs = FileSystem.SYSTEM
    val ts = Clock.System.now().epochSeconds
    for (ext in DB_EXTENSIONS) {
        try {
            val src = "$from$ext".toPath()
            if (!fs.exists(src)) continue
            val dst = "$to$ext".toPath()
            // Une cible existante est elle-même conservée (renommée), jamais supprimée
            if (fs.exists(dst)) fs.atomicMove(dst, "$to$ext.old.$ts".toPath())
            fs.atomicMove(src, dst)
        } catch (_: Exception) {}
    }
}

/**
 * Protège les données lorsqu'une application (ou une branche de développement) plus ancienne ouvre
 * une base créée par une version plus récente. Room ne sait pas « rétrograder » un schéma : sans
 * cette protection, la base était vidée, puis la sauvegarde .bak écrasée au démarrage suivant.
 *
 * - Base plus récente que l'application : elle est mise de côté sous `<db>.v<N>` (intacte).
 * - Base mise de côté pour la version de l'application (`<db>.v<app>`) alors que la base courante
 *   est absente ou plus ancienne : elle est restaurée, et la base courante mise de côté à son tour.
 *
 * Aucune donnée n'est supprimée : chaque version retrouve sa propre base.
 *
 * @return un message décrivant l'opération, ou null si rien n'a été fait
 */
fun protectDatabaseAgainstVersionChange(dbPath: String, appVersion: Int): String? {
    val fs = FileSystem.SYSTEM
    val current = readDatabaseVersion(dbPath)
    val parkedForApp = parkedDatabasePath(dbPath, appVersion)
    val hasParkedForApp = fs.exists(parkedForApp.toPath())

    return when {
        current != null && current > appVersion -> {
            moveDatabaseFiles(dbPath, parkedDatabasePath(dbPath, current))
            if (hasParkedForApp) moveDatabaseFiles(parkedForApp, dbPath)
            "Base de version $current mise de côté (${parkedDatabasePath(dbPath, current)})" +
                    if (hasParkedForApp) " ; base de version $appVersion restaurée" else ""
        }
        hasParkedForApp && (current == null || current < appVersion) -> {
            if (current != null) moveDatabaseFiles(dbPath, parkedDatabasePath(dbPath, current))
            moveDatabaseFiles(parkedForApp, dbPath)
            "Base de version $appVersion restaurée" +
                    if (current != null) " ; base de version $current mise de côté" else ""
        }
        else -> null
    }
}
