package fr.vetbrain.vetnutri_mp.DataBase

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Vérifie la protection contre l'écrasement des fichiers de base de données :
 * - `backupDatabaseFiles` doit copier (jamais déplacer) la DB + WAL/SHM avant migration.
 * - `rotateCorruptDatabaseFiles` doit déplacer (jamais supprimer) une base corrompue,
 *   pour permettre une restauration manuelle ultérieure depuis le `.bak` ou le fichier
 *   `.corrupt.<epoch>`.
 */
class DatabaseGuardTest {

    private lateinit var tempDir: File
    private lateinit var dbPath: String

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-guard-test").toFile()
        dbPath = File(tempDir, "vetnutri.db").absolutePath
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun backupDatabaseFiles_copiesMainDbFile_withoutRemovingOriginal() {
        File(dbPath).writeText("original-data")

        backupDatabaseFiles(dbPath)

        assertTrue(File(dbPath).exists(), "le fichier original ne doit jamais être supprimé")
        assertEquals("original-data", File(dbPath).readText())
        assertTrue(File("$dbPath.bak").exists())
        assertEquals("original-data", File("$dbPath.bak").readText())
    }

    @Test
    fun backupDatabaseFiles_copiesWalAndShmSidecars_whenPresent() {
        File(dbPath).writeText("main")
        File("$dbPath-wal").writeText("wal-data")
        File("$dbPath-shm").writeText("shm-data")

        backupDatabaseFiles(dbPath)

        assertTrue(File("$dbPath-wal.bak").exists())
        assertEquals("wal-data", File("$dbPath-wal.bak").readText())
        assertTrue(File("$dbPath-shm.bak").exists())
        assertEquals("shm-data", File("$dbPath-shm.bak").readText())
    }

    @Test
    fun backupDatabaseFiles_overwritesPreviousBakWithLatestData() {
        File(dbPath).writeText("first-run")
        backupDatabaseFiles(dbPath)
        assertEquals("first-run", File("$dbPath.bak").readText())

        File(dbPath).writeText("second-run")
        backupDatabaseFiles(dbPath)

        assertEquals("second-run", File("$dbPath.bak").readText())
    }

    @Test
    fun backupDatabaseFiles_missingFiles_doesNotThrowAndCreatesNoBackup() {
        // Aucun fichier DB n'existe encore (premier lancement)
        backupDatabaseFiles(dbPath)

        assertFalse(File("$dbPath.bak").exists())
        assertFalse(File("$dbPath-wal.bak").exists())
        assertFalse(File("$dbPath-shm.bak").exists())
    }

    @Test
    fun isDatabaseReadable_missingFile_isValidForFirstLaunch() {
        assertTrue(isDatabaseReadable(dbPath))
        assertFalse(File(dbPath).exists(), "le contrôle ne doit pas créer la base avant Room")
    }

    @Test
    fun isDatabaseReadable_rejectsNonSqliteFile() {
        File(dbPath).writeText("not a real sqlite database file")

        assertFalse(isDatabaseReadable(dbPath))
    }

    @Test
    fun rotateCorruptDatabaseFiles_movesMainFile_leavingOriginalPathFree() {
        File(dbPath).writeText("corrupt-data")

        rotateCorruptDatabaseFiles(dbPath)

        assertFalse(File(dbPath).exists(), "le chemin principal doit être libéré pour que Room recrée une base vide")
        val rotated = tempDir.listFiles { f -> f.name.startsWith("vetnutri.db.corrupt.") }
        assertEquals(1, rotated?.size)
        assertEquals("corrupt-data", rotated!!.first().readText())
    }

    @Test
    fun rotateCorruptDatabaseFiles_preservesExistingBakFile() {
        File(dbPath).writeText("original")
        backupDatabaseFiles(dbPath)
        File(dbPath).writeText("corrupted-after-backup")

        rotateCorruptDatabaseFiles(dbPath)

        // Le .bak (dernière sauvegarde saine) reste disponible pour restauration manuelle
        assertTrue(File("$dbPath.bak").exists())
        assertEquals("original", File("$dbPath.bak").readText())
    }

    @Test
    fun rotateCorruptDatabaseFiles_movesWalAndShmSidecars() {
        File(dbPath).writeText("main")
        File("$dbPath-wal").writeText("wal")
        File("$dbPath-shm").writeText("shm")

        rotateCorruptDatabaseFiles(dbPath)

        assertFalse(File("$dbPath-wal").exists())
        assertFalse(File("$dbPath-shm").exists())
        assertTrue(tempDir.listFiles { f -> f.name.startsWith("vetnutri.db-wal.corrupt.") }?.isNotEmpty() == true)
        assertTrue(tempDir.listFiles { f -> f.name.startsWith("vetnutri.db-shm.corrupt.") }?.isNotEmpty() == true)
    }

    @Test
    fun rotateCorruptDatabaseFiles_missingFiles_doesNotThrow() {
        rotateCorruptDatabaseFiles(dbPath)

        assertTrue(tempDir.listFiles()?.isEmpty() != false)
    }
}
