package fr.vetbrain.vetnutri_mp.Service

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.toApi
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import java.io.File
import java.sql.DriverManager
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LegacyV2FoodMigrationTest {

    private lateinit var db: AppDatabase
    private lateinit var legacyDir: File

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        legacyDir = Files.createTempDirectory("vetnutri-v2-migration-test").toFile()
        Class.forName("org.sqlite.JDBC")
    }

    @AfterTest
    fun tearDown() {
        db.close()
        legacyDir.deleteRecursively()
    }

    @Test
    fun runV2Migration_preservesEveryFoodFieldAndBiblioLink() = runTest {
        createLegacyFoodDatabase()
        createLegacyReferenceDatabase()

        val result = runV2Migration(legacyDir.absolutePath, db) {}

        assertTrue(result.errors.isEmpty(), result.errors.joinToString())
        assertEquals(1, result.imported.foods)
        assertEquals(1, result.imported.biblioRefs)

        val entity = db.foodDao().getFoodById("food-1")!!
        assertEquals(3, entity.groupAlim)
        assertEquals(1, entity.typeAlim)
        assertEquals("Poulet, riz", entity.ingredients)
        assertEquals(12.34, entity.price)
        assertEquals("Premium", entity.categPrice)
        assertEquals("Marque V2", entity.brand)
        assertEquals("Gamme V2", entity.gamme)
        assertEquals("CAN", entity.cont)
        assertEquals(7, entity.unitPres)
        assertEquals(395.0, entity.quantityPres)
        assertEquals(4, entity.version)
        assertEquals("2024-05-01", entity.date)
        assertEquals("Nom V2", entity.nameDef)
        assertEquals(1, entity.consistent)
        assertEquals(1, entity.deprecated)
        assertEquals("VF2024", entity.DataB)
        assertEquals("2026-07-31T10:20:30Z", entity.lastUpdateDate)
        assertEquals("images/food-1.png", entity.imageRef)

        assertEquals(
            listOf("biblio-1"),
            db.alimentBiblioRefDao().getBiblioRefUuids("food-1")
        )

        val repository = DatabaseFoodRepository(
            foodDao = db.foodDao(),
            nutrientValueDao = db.nutrientValueDao(),
            customNutrientDao = db.customNutrientDao(),
            alimentBiblioRefDao = db.alimentBiblioRefDao(),
            biblioRefDao = db.biblioRefDao(),
            energyPerSpeciesDao = db.energyPerSpeciesDao()
        )
        val exported = repository.getAllFoods().single().toApi()
        assertEquals(listOf("biblio-1"), exported.biblioRefIds)
        assertEquals("2026-07-31T10:20:30Z", exported.lastUpdateDate)
        assertEquals("images/food-1.png", exported.imageRef)
        assertTrue(exported.consistent == true)
    }

    private fun createLegacyFoodDatabase() {
        DriverManager.getConnection(
            "jdbc:sqlite:${File(legacyDir, "Data-Food.db").absolutePath}"
        ).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE FOOD (
                        UUID TEXT PRIMARY KEY,
                        groupAlim INTEGER,
                        typeAlim INTEGER,
                        ingredients TEXT,
                        price REAL,
                        categPrice TEXT,
                        brand TEXT,
                        gamme TEXT,
                        cont TEXT,
                        unitPres INTEGER,
                        quantityPres REAL,
                        version INTEGER,
                        date TEXT,
                        nameDef TEXT,
                        consistent INTEGER,
                        deprecated INTEGER,
                        DataB TEXT,
                        lastUpdateDate TEXT,
                        imageRef TEXT,
                        refBiblio TEXT
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    INSERT INTO FOOD VALUES (
                        'food-1', 3, 1, 'Poulet, riz', 12.34, 'Premium',
                        'Marque V2', 'Gamme V2', 'CAN', 7, 395.0, 4,
                        '2024-05-01', 'Nom V2', 1, 1, 'VF2024',
                        '2026-07-31T10:20:30Z', 'images/food-1.png', 'biblio-1'
                    )
                    """.trimIndent()
                )
            }
        }
    }

    private fun createLegacyReferenceDatabase() {
        DriverManager.getConnection(
            "jdbc:sqlite:${File(legacyDir, "ref.db").absolutePath}"
        ).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE BIBLIO_REFS (
                        UUID TEXT PRIMARY KEY,
                        fAuthor TEXT,
                        year INTEGER,
                        fullRef TEXT,
                        comments TEXT,
                        consistent INTEGER
                    )
                    """.trimIndent()
                )
                statement.execute(
                    """
                    INSERT INTO BIBLIO_REFS VALUES (
                        'biblio-1', 'Auteur V2', 2024,
                        'Référence bibliographique V2', 'Commentaire V2', 1
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
