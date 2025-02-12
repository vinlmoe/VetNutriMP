package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class FoodDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var foodDao: FoodDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
        foodDao = database.foodDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetFood() = runTest {
        val food =
                FoodEntity(
                        UUID = "test-uuid",
                        groupAlim = 1,
                        typeAlim = 1,
                        ingredients = "Test ingredients",
                        price = 10.0,
                        categPrice = "Test category",
                        brand = "Test brand",
                        gamme = "Test gamme",
                        unitPres = 1,
                        quantityPres = 100f,
                        version = 1,
                        date = "2024-03-20",
                        nameDef = "Test food",
                        consistent = 1,
                        deprecated = 0,
                        DataB = "Test DB"
                )

        foodDao.insertFood(food)

        val retrievedFood = foodDao.getFoodById("test-uuid")
        assertNotNull(retrievedFood)
        assertEquals(food.UUID, retrievedFood.UUID)
        assertEquals(food.nameDef, retrievedFood.nameDef)
    }

    @Test
    fun deleteFood() = runTest {
        val food =
                FoodEntity(
                        UUID = "test-uuid",
                        groupAlim = 1,
                        typeAlim = 1,
                        ingredients = "Test ingredients",
                        price = 10.0,
                        categPrice = "Test category",
                        brand = "Test brand",
                        gamme = "Test gamme",
                        unitPres = 1,
                        quantityPres = 100f,
                        version = 1,
                        date = "2024-03-20",
                        nameDef = "Test food",
                        consistent = 1,
                        deprecated = 0,
                        DataB = "Test DB"
                )

        foodDao.insertFood(food)
        foodDao.deleteFood(food)

        val retrievedFood = foodDao.getFoodById("test-uuid")
        assertNull(retrievedFood)
    }

    @Test
    fun getAllFood() = runTest {
        val foods =
                listOf(
                        FoodEntity(
                                UUID = "1",
                                groupAlim = 1,
                                typeAlim = 1,
                                ingredients = "Test ingredients 1",
                                price = 10.0,
                                categPrice = "Test category",
                                brand = "Test brand",
                                gamme = "Test gamme",
                                unitPres = 1,
                                quantityPres = 100f,
                                version = 1,
                                date = "2024-03-20",
                                nameDef = "Test food 1",
                                consistent = 1,
                                deprecated = 0,
                                DataB = "Test DB"
                        ),
                        FoodEntity(
                                UUID = "2",
                                groupAlim = 2,
                                typeAlim = 2,
                                ingredients = "Test ingredients 2",
                                price = 20.0,
                                categPrice = "Test category 2",
                                brand = "Test brand 2",
                                gamme = "Test gamme 2",
                                unitPres = 2,
                                quantityPres = 200f,
                                version = 1,
                                date = "2024-03-20",
                                nameDef = "Test food 2",
                                consistent = 1,
                                deprecated = 0,
                                DataB = "Test DB"
                        )
                )

        foods.forEach { foodDao.insertFood(it) }

        val allFoods = foodDao.getAllFood().first()
        assertEquals(2, allFoods.size)
    }

    @Test
    fun insertAndGetFoodName() = runTest {
        val foodName = NameFoodEntity(refFood = "food-uuid", lang = "FR", value = "Nom Test")

        foodDao.insertFoodName(foodName)

        val retrievedName = foodDao.getFoodName("food-uuid", "FR")
        assertNotNull(retrievedName)
        assertEquals(foodName.value, retrievedName.value)
    }

    @Test
    fun insertAndGetNutritionalValues() = runTest {
        val valueAA =
                ValueAAEntity(
                        kind = 1,
                        refFood = "food-uuid",
                        version = 1,
                        value = 10f,
                        date = "2024-03-20"
                )

        foodDao.insertValueAA(valueAA)

        val values = foodDao.getAAValues("food-uuid", 1)
        assertEquals(1, values.size)
        assertEquals(valueAA.value, values[0].value)
    }
}
