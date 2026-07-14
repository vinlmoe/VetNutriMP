package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Vérifie `DatabaseVersionManager.compareVersions`, la logique qui décide si le JSON de
 * données embarqué (`vetnutri_export_init.json`) doit être proposé en réimport au démarrage
 * (cf. StartupScreen.kt `isJsonUpdateNeeded`/`jsonUpdateAvailable`). Une erreur ici ferait soit
 * réafficher le popup de mise à jour à chaque lancement (comparaison trop stricte), soit ne
 * jamais proposer de vraie mise à jour (comparaison trop laxiste).
 *
 * Seule cette fonction pure est testée ici : `isJsonUpdateNeeded`/`getStoredJsonVersion`
 * passent par `createPreferencesStorage()`, qui écrit dans un fichier partagé avec la vraie
 * application (`~/.vetnutri_preferences.properties` côté desktop) et n'est pas isolable sans
 * modifier PreferencesStorage pour accepter un emplacement injecté.
 */
class DatabaseVersionManagerTest {

    private val manager = DatabaseVersionManager()

    @Test
    fun compareVersions_identicalVersions_returnsZero() {
        assertEquals(0, manager.compareVersions("2.5.1", "2.5.1"))
    }

    @Test
    fun compareVersions_higherMajor_returnsPositive() {
        assertEquals(1, manager.compareVersions("3.0.0", "2.9.9"))
    }

    @Test
    fun compareVersions_higherMinor_doesNotMisreadAsLexicographic() {
        // Piège classique : "2.10" doit être supérieur à "2.9" (comparaison numérique par
        // segment), pas inférieur comme le donnerait une comparaison de chaînes.
        assertEquals(1, manager.compareVersions("2.10.0", "2.9.0"))
    }

    @Test
    fun compareVersions_missingTrailingSegments_treatedAsZero() {
        // "1.2" doit être équivalent à "1.2.0"
        assertEquals(0, manager.compareVersions("1.2", "1.2.0"))
    }

    @Test
    fun compareVersions_lowerVersion_returnsNegative() {
        assertEquals(-1, manager.compareVersions("1.0.0", "1.0.1"))
    }

    @Test
    fun compareVersions_nonNumericSegments_treatedAsZeroRatherThanThrowing() {
        assertEquals(0, manager.compareVersions("1.x.0", "1.0.0"))
    }

    @Test
    fun formatVersion_prependsV() {
        assertEquals("v1.2.3", manager.formatVersion("1.2.3"))
    }
}
