package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Vérifie que `getRoomDatabase` (utilisée par toutes les plateformes au démarrage) :
 * - persiste réellement les données sur disque entre deux ouvertures successives ;
 * - ne déclenche jamais de perte de données lors d'une réouverture normale (régression
 *   contre la réintroduction de `fallbackToDestructiveMigration`, cf. commentaire dans
 *   AppDatabase.kt) ;
 * - conserve le fichier corrompu (jamais de suppression silencieuse) lorsque l'ouverture
 *   échoue, tout en permettant à l'application de redémarrer avec une base vide utilisable.
 */
class AppDatabasePersistenceTest {

    private lateinit var tempDir: File
    private lateinit var dbPath: String

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-persistence-test").toFile()
        dbPath = File(tempDir, "vetnutri.db").absolutePath
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun freshBuilder() = Room.databaseBuilder<AppDatabase>(name = dbPath)

    private fun animal(uuid: String, nom: String) = AnimalEntity(
        uuid = uuid,
        nom = nom,
        id = null,
        specieId = null,
        ownerName = null,
        birthdate = null,
        race = null,
        summary = null
    )

    @Test
    fun getRoomDatabase_persistsDataAcrossReopenOnSamePath() = runTest {
        val db1 = getRoomDatabase(freshBuilder(), dbPath)
        db1.animalDao().insert(animal("a1", "Rex"))
        db1.close()

        val db2 = getRoomDatabase(freshBuilder(), dbPath)
        val reloaded = db2.animalDao().getAnimalById("a1")
        db2.close()

        assertNotNull(reloaded, "les données écrites lors de la première ouverture doivent survivre à la réouverture")
        assertEquals("Rex", reloaded.nom)
    }

    @Test
    fun getRoomDatabase_reopeningHealthyDatabase_neverDestroysExistingData() = runTest {
        val db1 = getRoomDatabase(freshBuilder(), dbPath)
        db1.animalDao().insert(animal("a1", "Rex"))
        db1.animalDao().insert(animal("a2", "Milo"))
        db1.close()

        // Simule plusieurs redémarrages successifs de l'application sur la même base
        repeat(3) {
            val db = getRoomDatabase(freshBuilder(), dbPath)
            assertEquals(2, db.animalDao().getAllAnimals().size, "aucune donnée ne doit être perdue entre deux ouvertures")
            db.close()
        }
    }

    @Test
    fun getRoomDatabase_backsUpFilesBeforeOpening() = runTest {
        val db1 = getRoomDatabase(freshBuilder(), dbPath)
        db1.animalDao().insert(animal("a1", "Rex"))
        db1.close()

        // Deuxième ouverture : getRoomDatabase doit avoir copié la base v1 en .bak avant de la rouvrir
        val db2 = getRoomDatabase(freshBuilder(), dbPath)
        db2.close()

        assertTrue(File("$dbPath.bak").exists(), "une sauvegarde .bak doit être créée avant toute (ré)ouverture")
    }

    @Test
    fun getRoomDatabase_corruptedFile_rotatesItInsteadOfDeletingAndOpensCleanDatabase() = runTest {
        // Fichier non-SQLite : l'ouverture Room doit échouer et déclencher la rotation,
        // jamais une suppression silencieuse des données existantes.
        File(dbPath).writeText("not a real sqlite database file")

        val db = getRoomDatabase(freshBuilder(), dbPath)

        // L'app peut continuer avec une base vide fonctionnelle...
        assertEquals(0, db.animalDao().getAllAnimals().size)
        db.close()

        // ...mais le fichier corrompu original doit avoir été préservé, pas effacé.
        val rotated = tempDir.listFiles { f -> f.name.startsWith("vetnutri.db.corrupt.") }
        assertTrue(rotated?.isNotEmpty() == true, "le fichier corrompu doit être renommé, jamais supprimé")
        assertEquals("not a real sqlite database file", rotated!!.first().readText())
    }

    @Test
    fun checkIntegrity_returnsTrue_forHealthyDatabase() = runTest {
        val db = getRoomDatabase(freshBuilder(), dbPath)
        db.animalDao().insert(animal("a1", "Rex"))

        assertTrue(db.checkIntegrity())

        db.close()
    }
}
