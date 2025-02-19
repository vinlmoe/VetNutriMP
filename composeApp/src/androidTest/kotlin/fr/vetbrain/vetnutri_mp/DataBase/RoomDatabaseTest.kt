package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var animalDao: AnimalDao
    private lateinit var foodDao: FoodDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
        animalDao = db.animalDao()
        foodDao = db.foodDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testFoodEntityInsertion() = runBlocking {
        // Création d'un FoodEntity
        val foodEntity =
                FoodEntity(
                        UUID = "test-uuid",
                        groupAlim = 1,
                        typeAlim = 2,
                        ingredients = "Test ingredients",
                        price = 10.0,
                        categPrice = "Premium",
                        brand = "Test Brand",
                        gamme = "Test Gamme",
                        unitPres = 1,
                        quantityPres = 100f,
                        version = 1,
                        date = LocalDate(2024, 3, 15).toString(),
                        nameDef = "Test Food",
                        consistent = 1,
                        deprecated = 0,
                        DataB = "Test DB"
                )

        // Insertion dans la base de données
        foodDao.insertFood(foodEntity)

        // Vérification de l'insertion
        val retrievedFood = foodDao.getFoodByUuid(foodEntity.UUID)
        assertNotNull(retrievedFood)
        assertEquals(foodEntity.nameDef, retrievedFood.nameDef)
    }

    @Test
    fun testAlimentRationEntityInsertion() = runBlocking {
        // Création d'une ration
        val rationEntity =
                RationEntity(
                        uuid = "ration-uuid",
                        idConsult = "consult-uuid",
                        name = "Test Ration",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test Description"
                )

        // Création d'un aliment dans la ration
        val alimentRationEntity =
                AlimentRationEntity(
                        uuid = "aliment-uuid",
                        refAlimUnif = "food-uuid",
                        refRation = rationEntity.uuid,
                        quantity = 150f,
                        refTarget = 1
                )

        // Insertion dans la base de données
        foodDao.insertRation(rationEntity)
        foodDao.insertAlimentRation(alimentRationEntity)

        // Vérification des insertions
        val retrievedRation = foodDao.getRationByUuid(rationEntity.uuid)
        assertNotNull(retrievedRation)
        assertEquals(rationEntity.name, retrievedRation.name)

        val retrievedAliment = foodDao.getAlimentRationByUuid(alimentRationEntity.uuid)
        assertNotNull(retrievedAliment)
        assertEquals(alimentRationEntity.quantity, retrievedAliment.quantity)
    }
}
