package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import java.io.File

actual fun testNasDbPath(path: String): NasTestResult {
    if (path.isBlank()) return NasTestResult.EmptyPath
    val file = File(path)
    val parent = file.parentFile
    if (parent == null || !parent.exists() || !parent.canRead()) return NasTestResult.Inaccessible
    if (file.exists()) {
        val version = NasDatabaseChecker.readSqliteUserVersion(path)
            ?: return NasTestResult.Ok
        if (version > AppDatabase.DATABASE_VERSION)
            return NasTestResult.VersionTooHigh(version)
    }
    return NasTestResult.Ok
}
