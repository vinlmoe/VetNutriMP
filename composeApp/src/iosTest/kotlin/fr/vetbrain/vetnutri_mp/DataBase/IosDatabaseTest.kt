package fr.vetbrain.vetnutri_mp.DataBase

import kotlin.test.*
import kotlinx.cinterop.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.SQLite.*

class IosDatabaseTest {
    private lateinit var database: CPointer<sqlite3>
    private lateinit var animalDao: IosAnimalDao
    private lateinit var foodDao: IosFoodDao

    @BeforeTest
    fun setup() {
        memScoped {
            val dbPtr = alloc<CPointerVar<sqlite3>>()
            val result =
                    sqlite3_open_v2(
                            ":memory:",
                            dbPtr.ptr,
                            SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE,
                            null
                    )

            if (result != SQLITE_OK) {
                fail("Impossible d'ouvrir la base de données en mémoire")
            }

            database = dbPtr.value!!

            // Création des tables
            val createAnimalTable =
                    """
                CREATE TABLE IF NOT EXISTS animals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    species TEXT NOT NULL,
                    birth_date TEXT NOT NULL
                )
            """.trimIndent()

            val createFoodTable =
                    """
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT NOT NULL,
                    category TEXT NOT NULL
                )
            """.trimIndent()

            val stmt = alloc<CPointerVar<sqlite3_stmt>>()

            if (sqlite3_prepare_v2(database, createAnimalTable, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            } else {
                fail("Impossible de créer la table animals")
            }

            if (sqlite3_prepare_v2(database, createFoodTable, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            } else {
                fail("Impossible de créer la table foods")
            }
        }

        animalDao = IosAnimalDao(database)
        foodDao = IosFoodDao(database)
    }

    @AfterTest
    fun cleanup() {
        sqlite3_close(database)
    }

    @Test
    fun testAnimalCRUD() = runTest {
        // Test création
        val animal = Animal(name = "Rex", species = "Chien", birthDate = "2020-01-01")

        val id = animalDao.insertAnimal(animal)
        assertTrue(id > 0)

        // Test lecture
        val retrievedAnimal = animalDao.getAnimalById(id)
        assertNotNull(retrievedAnimal)
        assertEquals(animal.name, retrievedAnimal?.name)

        // Test mise à jour
        val updatedAnimal = animal.copy(id = id, name = "Max")
        animalDao.updateAnimal(updatedAnimal)

        val retrievedUpdatedAnimal = animalDao.getAnimalById(id)
        assertEquals("Max", retrievedUpdatedAnimal?.name)

        // Test suppression
        animalDao.deleteAnimal(id)
        val deletedAnimal = animalDao.getAnimalById(id)
        assertNull(deletedAnimal)
    }

    @Test
    fun testFoodCRUD() = runTest {
        // Test création
        val food =
                Food(
                        name = "Croquettes Premium",
                        description = "Croquettes haut de gamme pour chiens",
                        category = "Croquettes"
                )

        val id = foodDao.insertFood(food)
        assertTrue(id > 0)

        // Test lecture
        val retrievedFood = foodDao.getFoodById(id).first()
        assertNotNull(retrievedFood)
        assertEquals(food.name, retrievedFood?.name)

        // Test mise à jour
        val updatedFood = food.copy(id = id, name = "Croquettes Premium Plus")
        foodDao.updateFood(updatedFood)

        val retrievedUpdatedFood = foodDao.getFoodById(id).first()
        assertEquals("Croquettes Premium Plus", retrievedUpdatedFood?.name)

        // Test suppression
        foodDao.deleteFood(id)
        val deletedFood = foodDao.getFoodById(id).first()
        assertNull(deletedFood)
    }
}
