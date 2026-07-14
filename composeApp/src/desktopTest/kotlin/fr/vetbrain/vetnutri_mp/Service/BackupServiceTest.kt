package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.InMemoryAnimalRepository
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Vérifie que `BackupService` (sauvegarde JSON automatique/manuelle) ne perd pas de données
 * lors d'un cycle sauvegarde/restauration, et que la rotation des fichiers de sauvegarde ne
 * dépasse jamais la limite prévue sans laisser de fichiers de métadonnées orphelins.
 *
 * `FileService` (desktop) écrit dans `~/.vetnutri_mp/backups`, un chemin réel et partagé avec
 * l'application. Pour ne jamais toucher aux vraies sauvegardes de l'utilisateur qui lancerait
 * ces tests localement, la propriété système `user.home` est redirigée vers un répertoire
 * temporaire pendant toute la durée du test puis restaurée.
 */
class BackupServiceTest {

    private lateinit var tempDir: File
    private lateinit var originalUserHome: String

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-backup-test").toFile()
        originalUserHome = System.getProperty("user.home")
        System.setProperty("user.home", tempDir.absolutePath)
    }

    @AfterTest
    fun tearDown() {
        System.setProperty("user.home", originalUserHome)
        tempDir.deleteRecursively()
    }

    private fun newBackupService(animalRepository: InMemoryAnimalRepository): BackupService {
        val exportImportRepository = ExportImportRepository(animalRepository)
        return BackupService(exportImportRepository, FileService())
    }

    @Test
    fun createBackup_thenRestoreOnFreshRepository_roundTripsAnimalData() = runTest {
        val sourceRepo = InMemoryAnimalRepository()
        sourceRepo.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex", ownerName = "Jean Dupont"))
        val backupService = newBackupService(sourceRepo)

        val createResult = backupService.createBackup()
        assertTrue(createResult.isSuccess, "la création de sauvegarde doit réussir")
        assertEquals(1, createResult.getOrThrow().animalCount)

        // Nouvelle "installation" vide, mais qui lit le même répertoire de sauvegardes
        val targetRepo = InMemoryAnimalRepository()
        val restoreService = newBackupService(targetRepo)

        val backups = restoreService.getAvailableBackups()
        assertEquals(1, backups.size)

        val restoreResult = restoreService.restoreBackup(backups.first())
        assertTrue(restoreResult.isSuccess, "la restauration doit réussir")
        assertEquals(1, restoreResult.getOrThrow().animals)

        val restoredAnimal = targetRepo.getAnimalById("a1")
        assertEquals("Rex", restoredAnimal?.nom)
        assertEquals("Jean Dupont", restoredAnimal?.ownerName)

        backupService.cleanup()
        restoreService.cleanup()
    }

    @Test
    fun manageBackupRotation_neverExceedsMaxBackupFiles_andLeavesNoOrphanMetadata() = runTest {
        val repo = InMemoryAnimalRepository()
        val backupService = newBackupService(repo)

        // MAX_BACKUP_FILES = 10 côté BackupService : on en crée davantage pour déclencher
        // la rotation plusieurs fois de suite.
        repeat(13) { i ->
            repo.saveAnimal(AnimalEv(uuid = "a$i", nom = "Animal$i"))
            val result = backupService.createBackup()
            assertTrue(result.isSuccess, "la sauvegarde #$i doit réussir")
            // Garantit des timestamps distincts pour chaque nom de fichier de sauvegarde.
            Thread.sleep(5)
        }

        val backups = backupService.getAvailableBackups()
        assertEquals(10, backups.size, "la rotation ne doit jamais garder plus de 10 sauvegardes")

        val backupDir = File(tempDir, ".vetnutri_mp/backups")
        val jsonFiles = backupDir.listFiles { f -> f.name.endsWith(".json") && !f.name.contains("_metadata") }
        val metadataFiles = backupDir.listFiles { f -> f.name.endsWith("_metadata.json") }
        assertEquals(10, jsonFiles?.size, "aucun fichier de sauvegarde orphelin ne doit rester après rotation")
        assertEquals(10, metadataFiles?.size, "aucune métadonnée orpheline ne doit rester après rotation")

        backupService.cleanup()
    }
}
