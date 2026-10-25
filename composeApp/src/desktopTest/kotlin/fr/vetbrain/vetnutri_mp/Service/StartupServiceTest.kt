package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.InMemoryAnimalRepository
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * `StartupService.initialize()` : crée le répertoire de sauvegarde et lance la sauvegarde
 * automatique (`BackupService.startAutomaticBackup`), qui écrit une première sauvegarde
 * immédiatement puis une toutes les dix minutes si la base a changé.
 *
 * Comme [BackupServiceTest], `user.home` est redirigé vers un répertoire temporaire pour ne jamais
 * toucher aux vraies sauvegardes (`~/.vetnutri_mp/backups`). La sauvegarde automatique tourne sur
 * le dispatcher IO réel : les tests attendent (avec délai borné) l'apparition des fichiers.
 */
class StartupServiceTest {

    private lateinit var tempDir: File
    private lateinit var originalUserHome: String
    private val services = mutableListOf<StartupService>()

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-startup-test").toFile()
        originalUserHome = System.getProperty("user.home")
        System.setProperty("user.home", tempDir.absolutePath)
    }

    @AfterTest
    fun tearDown() {
        services.forEach { it.shutdown() }
        System.setProperty("user.home", originalUserHome)
        tempDir.deleteRecursively()
    }

    private val backupDir: File
        get() = File(tempDir, ".vetnutri_mp/backups")

    /** Fichiers de sauvegarde (hors métadonnées et compteur). */
    private fun fichiersSauvegarde(): List<File> =
            backupDir.listFiles { f ->
                f.name.startsWith("vetnutri_backup_") && f.name.endsWith(".json") && !f.name.contains("_metadata")
            }?.toList() ?: emptyList()

    /** Attend (au plus [timeoutMs]) que le nombre de sauvegardes atteigne [attendu]. */
    private fun attendreSauvegardes(attendu: Int, timeoutMs: Long = 15_000): Int {
        val limite = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < limite) {
            if (fichiersSauvegarde().size >= attendu) break
            Thread.sleep(50)
        }
        return fichiersSauvegarde().size
    }

    private fun nouveauService(animalRepository: InMemoryAnimalRepository): StartupService {
        val service = StartupService(ExportImportRepository(animalRepository), FileService())
        services += service
        return service
    }

    private fun marquerChangementBase() {
        DatabaseChangeNotifier.notifyChange(DatabaseChangeNotifier.ChangeType.ANIMAL_UPDATED, "test")
    }

    @Test
    fun initialize_creeLeRepertoireEtLanceUneSauvegardeAutomatique() = runBlocking {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex"))
        marquerChangementBase()
        val service = nouveauService(repo)

        service.initialize()

        assertTrue(backupDir.isDirectory, "le répertoire de sauvegarde doit être créé")
        assertEquals(1, attendreSauvegardes(1), "une première sauvegarde automatique doit être écrite au démarrage")
        val backups = service.getAvailableBackups()
        assertEquals(1, backups.size)
        assertEquals(1, backups.single().animalCount)
        assertNotNull(service.getBackupService())
        assertSame(service.getBackupService(), service.getBackupService(), "un seul BackupService par StartupService")
    }

    @Test
    fun initialize_appeleDeuxFois_neDupliquePasLaSauvegarde() = runBlocking {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex"))
        marquerChangementBase()
        val service = nouveauService(repo)

        service.initialize()
        assertEquals(1, attendreSauvegardes(1))

        // Second appel : la tâche automatique est relancée, mais rien n'a changé en base depuis
        // la dernière sauvegarde → aucune nouvelle sauvegarde
        service.initialize()
        Thread.sleep(1_500)
        assertEquals(1, fichiersSauvegarde().size, "initialize() doit être idempotent sans changement en base")

        // Après un changement, la relance produit exactement une sauvegarde de plus
        repo.saveAnimal(AnimalEv(uuid = "a2", nom = "Milo"))
        marquerChangementBase()
        service.initialize()
        assertEquals(2, attendreSauvegardes(2), "une nouvelle sauvegarde après changement")
        Thread.sleep(1_000)
        assertEquals(2, fichiersSauvegarde().size, "pas de sauvegarde en double après relance")
        assertEquals(2, service.getAvailableBackups().first().animalCount)
    }

    @Test
    fun shutdown_arreteLaSauvegardeAutomatique() = runBlocking {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex"))
        marquerChangementBase()
        val service = nouveauService(repo)
        service.initialize()
        assertEquals(1, attendreSauvegardes(1))

        service.shutdown()
        marquerChangementBase()
        Thread.sleep(1_000)
        assertEquals(1, fichiersSauvegarde().size, "plus aucune sauvegarde automatique après shutdown()")
    }

    @Test
    fun createManualBackup_etFindRecoveryBackup() = runBlocking {
        val repo = InMemoryAnimalRepository()
        repo.saveAnimal(AnimalEv(uuid = "a1", nom = "Rex"))
        val service = nouveauService(repo)
        // Sans initialize() : aucune sauvegarde automatique, seule la sauvegarde manuelle existe
        val manuelle = service.createManualBackup()
        assertTrue(manuelle.isSuccess)
        assertEquals(1, fichiersSauvegarde().size)
        assertEquals(manuelle.getOrThrow().fileName, service.findRecoveryBackup()?.fileName)
    }
}
