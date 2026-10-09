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

    @Test
    fun restoreMergesWithoutDeletingNewAnimalsAndKeepsRecoveryBackup() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a", nom = "Original"))
        val service = newBackupService(repo)
        val original = service.createBackup().getOrThrow()
        repo.saveAnimal(AnimalEv(uuid = "a", nom = "Modified"))
        repo.saveAnimal(AnimalEv(uuid = "b", nom = "New"))
        assertTrue(service.restoreBackup(original).isSuccess)
        assertEquals("Original", repo.getAnimalById("a")?.nom)
        assertEquals("New", repo.getAnimalById("b")?.nom)
        val recovery = service.getAvailableBackups().single { it.fileName != original.fileName }
        assertEquals(2, recovery.animalCount)
        service.cleanup()
    }

    @Test
    fun missingMetadataIsReconstructedFromActualContent() = runTest {
        val repo = InMemoryAnimalRepository()
        repeat(3) { repo.saveAnimal(AnimalEv(uuid = "a$it", nom = "Animal$it")) }
        val service = newBackupService(repo)
        val backup = service.createBackup().getOrThrow()
        File(backup.filePath.replace(".json", "_metadata.json")).delete()
        val recovered = service.getAvailableBackups().single()
        assertEquals(3, recovered.animalCount)
        assertEquals(0, recovered.foodCount)
        assertEquals(0, recovered.equationCount)
        assertEquals(0, recovered.recipeCount)
        service.cleanup()
    }

    @Test
    fun malformedBackupDoesNotChangeDataOrCreateRecoveryBackup() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a", nom = "Keep"))
        val service = newBackupService(repo)
        val backup = service.createBackup().getOrThrow()
        File(backup.filePath).writeText("invalid json")
        assertTrue(service.restoreBackup(backup).isFailure)
        assertEquals("Keep", repo.getAnimalById("a")?.nom)
        assertEquals(1, service.getAvailableBackups().size)
        service.cleanup()
    }

    @Test
    fun importFailureIsNotReportedAsSuccessfulRestoration() = runTest {
        val source = InMemoryAnimalRepository()
        source.saveAnimal(AnimalEv(uuid = "a", nom = "Rex"))
        val sourceService = newBackupService(source)
        val backup = sourceService.createBackup().getOrThrow()
        val backing = InMemoryAnimalRepository()
        val failing = object : fr.vetbrain.vetnutri_mp.Repository.AnimalRepository by backing {
            override suspend fun saveAnimal(animal: AnimalEv) { error("write failed") }
        }
        val service = BackupService(ExportImportRepository(failing), FileService())
        val result = service.restoreBackup(backup)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Sauvegarde préalable"))
        assertEquals(2, service.getAvailableBackups().size)
        service.cleanup()
        sourceService.cleanup()
    }

    @Test
    fun failedRecoveryBackupPreventsImport() = runTest {
        val source = InMemoryAnimalRepository()
        source.saveAnimal(AnimalEv(uuid = "a", nom = "Rex"))
        val sourceService = newBackupService(source)
        val backup = sourceService.createBackup().getOrThrow()
        val backing = InMemoryAnimalRepository()
        var writes = 0
        val failing = object : fr.vetbrain.vetnutri_mp.Repository.AnimalRepository by backing {
            override suspend fun getAllAnimals(): List<AnimalEv> = error("read failed")
            override suspend fun saveAnimal(animal: AnimalEv) { writes++ }
        }
        val service = BackupService(ExportImportRepository(failing), FileService())
        assertTrue(service.restoreBackup(backup).isFailure)
        assertEquals(0, writes)
        service.cleanup()
        sourceService.cleanup()
    }

    @Test
    fun automaticBackupOfEmptyDatabaseKeepsBackupWithAnimals() = runTest {
        val source = InMemoryAnimalRepository()
        source.saveAnimal(AnimalEv(uuid = "a", nom = "Rex"))
        val sourceService = newBackupService(source)
        val withAnimals = sourceService.createBackup().getOrThrow()

        // Base vidée (perte de base) qui lit le même répertoire de sauvegardes
        val emptyService = newBackupService(InMemoryAnimalRepository())
        assertEquals(withAnimals.fileName, emptyService.findLatestBackupWithAnimals()?.fileName)
        repeat(12) { assertTrue(emptyService.createAutomaticBackup().isFailure) }
        assertEquals(1, emptyService.getAvailableBackups().size)
        assertEquals(withAnimals.fileName, emptyService.findLatestBackupWithAnimals()?.fileName)

        // Une sauvegarde manuelle reste possible
        assertTrue(emptyService.createBackup().isSuccess)
        emptyService.cleanup()
        sourceService.cleanup()
    }

    @Test
    fun automaticBackupOfEmptyDatabaseWithoutPreviousAnimalsIsCreated() = runTest {
        val service = newBackupService(InMemoryAnimalRepository())
        assertTrue(service.createAutomaticBackup().isSuccess)
        assertEquals(null, service.findLatestBackupWithAnimals())
        service.cleanup()
    }

    @Test
    fun everyHundredthAutomaticBackupIsKeptLongTermDespiteRotation() = runTest {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a", nom = "Rex"))
        val service = newBackupService(repo)

        repeat(BackupService.LONG_TERM_EVERY - 1) { assertTrue(service.createAutomaticBackup().isSuccess) }
        assertTrue(service.getAvailableBackups().none { it.isLongTerm })

        assertTrue(service.createAutomaticBackup().isSuccess)
        val longTerm = service.getAvailableBackups().filter { it.isLongTerm }
        assertEquals(1, longTerm.size)
        assertEquals(1, longTerm.single().animalCount)

        // La rotation continue sur les sauvegardes ordinaires sans toucher à la long terme
        repeat(15) { assertTrue(service.createAutomaticBackup().isSuccess) }
        val backups = service.getAvailableBackups()
        assertEquals(longTerm.single().fileName, backups.single { it.isLongTerm }.fileName)
        assertEquals(10, backups.count { !it.isLongTerm })
        assertTrue(service.restoreBackup(longTerm.single()).isSuccess)
        service.cleanup()
    }

    @Test
    fun missingSourceMoveIsFailure() = runTest {
        val missing = fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile(File(tempDir, "missing").path)
        val destination = fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile(File(tempDir, "destination").path)
        assertTrue(FileService().moveFile(missing, destination).isFailure)
    }
}
