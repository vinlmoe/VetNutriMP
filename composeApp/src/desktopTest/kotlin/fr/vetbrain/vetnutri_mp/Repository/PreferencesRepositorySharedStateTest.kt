package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import java.io.File
import java.nio.file.Files
import kotlin.test.*
import kotlinx.coroutines.test.runTest

/**
 * Les vues lisent les préférences via le flux de l'instance partagée : une sauvegarde faite dans
 * les Paramètres doit y apparaître sans rechargement, et le stockage n'est lu qu'une fois.
 */
class PreferencesRepositorySharedStateTest {
    private lateinit var home: String
    private lateinit var directory: File

    @BeforeTest fun prepare() {
        home = System.getProperty("user.home")
        directory = Files.createTempDirectory("vetnutri-preferences-repo-test").toFile()
        System.setProperty("user.home", directory.path)
    }
    @AfterTest fun cleanup() {
        System.setProperty("user.home", home)
        directory.deleteRecursively()
    }

    @Test fun flowIsNullUntilFirstLoad() = runTest {
        val repo = PreferencesRepository(PreferencesStorage())
        assertNull(repo.preferencesFlow.value)
        repo.getLoadedPreferences()
        assertNotNull(repo.preferencesFlow.value)
    }

    @Test fun savedPreferencesReachTheFlowWithoutReload() = runTest {
        val repo = PreferencesRepository(PreferencesStorage())
        val modifiees = repo.getLoadedPreferences().copy(nomUtilisateur = "Dr Martin")
        repo.savePreferences(modifiees)
        assertEquals("Dr Martin", repo.preferencesFlow.value?.nomUtilisateur)
        assertEquals("Dr Martin", repo.getLoadedPreferences().nomUtilisateur)
    }

    @Test fun storageIsReadOnlyOnce() = runTest {
        val storage = PreferencesStorage()
        val repo = PreferencesRepository(storage)
        repo.getLoadedPreferences()
        // Écriture directe sur le stockage : l'instance partagée garde sa copie déjà chargée
        PreferencesRepository(storage).savePreferences(
                repo.getLoadedPreferences().copy(nomUtilisateur = "Autre")
        )
        assertEquals("", repo.getLoadedPreferences().nomUtilisateur)
    }
}
