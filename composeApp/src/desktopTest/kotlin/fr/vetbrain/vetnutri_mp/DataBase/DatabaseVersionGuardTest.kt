package fr.vetbrain.vetnutri_mp.DataBase

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Bascules entre une version récente (ex. branche v37) et une plus ancienne (ex. main v36) :
 * aucune base ne doit être vidée ni perdue, chaque version retrouve la sienne.
 */
class DatabaseVersionGuardTest {

    private lateinit var dir: File
    private lateinit var dbPath: String

    @BeforeTest
    fun setUp() {
        dir = kotlin.io.path.createTempDirectory("dbguard").toFile()
        dbPath = File(dir, "vetnutri.db").absolutePath
    }

    @AfterTest
    fun tearDown() {
        dir.deleteRecursively()
    }

    /** Crée une base de la version donnée contenant une ligne marquée [marque]. */
    private fun creerBase(path: String, version: Int, marque: String) {
        BundledSQLiteDriver().open(path).use { c ->
            c.prepare("CREATE TABLE IF NOT EXISTS T (v TEXT)").use { it.step() }
            c.prepare("INSERT INTO T VALUES ('$marque')").use { it.step() }
            c.prepare("PRAGMA user_version = $version").use { it.step() }
        }
    }

    private fun marque(path: String): String =
            BundledSQLiteDriver().open(path).use { c ->
                c.prepare("SELECT v FROM T").use { st -> st.step(); st.getText(0) }
            }

    @Test
    fun ouvertureParVersionPlusAncienne_miseDeCote_puisRestaurationAuRetour() {
        creerBase(dbPath, 37, "donnees-v37")

        // main (v36) démarre : la base v37 est mise de côté, jamais vidée
        assertNotNull(protectDatabaseAgainstVersionChange(dbPath, 36))
        assertFalse(File(dbPath).exists())
        assertEquals("donnees-v37", marque(parkedDatabasePath(dbPath, 37)))

        // main crée sa propre base v36 et y travaille
        creerBase(dbPath, 36, "donnees-v36")

        // retour sur la branche v37 : sa base est restaurée, celle de main mise de côté
        assertNotNull(protectDatabaseAgainstVersionChange(dbPath, 37))
        assertEquals("donnees-v37", marque(dbPath))
        assertEquals("donnees-v36", marque(parkedDatabasePath(dbPath, 36)))
        assertFalse(File(parkedDatabasePath(dbPath, 37)).exists())

        // nouveau passage sur main : chacun retrouve sa base
        assertNotNull(protectDatabaseAgainstVersionChange(dbPath, 36))
        assertEquals("donnees-v36", marque(dbPath))
        assertEquals("donnees-v37", marque(parkedDatabasePath(dbPath, 37)))
    }

    @Test
    fun migrationNormale_rienNestDeplace() {
        creerBase(dbPath, 36, "donnees-v36")
        assertNull(protectDatabaseAgainstVersionChange(dbPath, 37))
        assertEquals("donnees-v36", marque(dbPath))
        assertNull(protectDatabaseAgainstVersionChange(dbPath, 36))
    }

    @Test
    fun premierLancement_sansBase_rienNestFait() {
        assertNull(protectDatabaseAgainstVersionChange(dbPath, 37))
        assertFalse(File(dbPath).exists())
    }

    @Test
    fun cibleDejaPresente_estConserveeEtNonEcrasee() {
        creerBase(parkedDatabasePath(dbPath, 37), 37, "ancienne-v37")
        File(parkedDatabasePath(dbPath, 37)).renameTo(File(parkedDatabasePath(dbPath, 37) + ".tmp"))
        creerBase(dbPath, 37, "recente-v37")
        File(parkedDatabasePath(dbPath, 37) + ".tmp").renameTo(File(parkedDatabasePath(dbPath, 37)))
        // La base courante v37 part de côté alors qu'un .v37 existe déjà : les deux sont gardés
        protectDatabaseAgainstVersionChange(dbPath, 36)
        assertEquals("recente-v37", marque(parkedDatabasePath(dbPath, 37)))
        assertTrue(dir.listFiles()!!.any { it.name.startsWith("vetnutri.db.v37.old.") })
    }
}
