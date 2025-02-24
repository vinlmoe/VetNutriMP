package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodRelationEntitiesTest {
    private lateinit var database: AndroidDatabase
    private lateinit var alimentDao: AlimentDao

    @Before
    fun createDb() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                AndroidDatabase::class.java
                        )
                        .build()
        alimentDao = database.alimentDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testFoodSpeciesRelation() = runBlocking {
        // Créer des entités de relation espèces
        val species1 = FoodSpeciesEntity("food-1", "CHIEN")
        val species2 = FoodSpeciesEntity("food-1", "CHAT")

        // Insérer via DAO
        database.foodSpeciesDao().insert(species1)
        database.foodSpeciesDao().insert(species2)

        // Récupérer les relations
        val relations = database.foodSpeciesDao().getSpeciesForFood("food-1")
        assertEquals(2, relations.size)
        assertTrue(relations.any { it.species == "CHIEN" })
        assertTrue(relations.any { it.species == "CHAT" })
    }

    @Test
    fun testFoodIndicationRelation() = runBlocking {
        // Créer des entités de relation indications
        val indication1 = FoodIndicationEntity("food-1", "PHYS")
        val indication2 = FoodIndicationEntity("food-1", "SEN")

        // Insérer via DAO
        database.foodIndicationDao().insert(indication1)
        database.foodIndicationDao().insert(indication2)

        // Récupérer les relations
        val relations = database.foodIndicationDao().getIndicationsForFood("food-1")
        assertEquals(2, relations.size)
        assertTrue(relations.any { it.indication == "PHYS" })
        assertTrue(relations.any { it.indication == "SEN" })
    }

    @Test
    fun testCascadeDelete() = runBlocking {
        // Créer un aliment avec des relations
        val foodId = "test-cascade"

        // Ajouter des relations
        database.foodSpeciesDao().insert(FoodSpeciesEntity(foodId, "CHIEN"))
        database.foodIndicationDao().insert(FoodIndicationEntity(foodId, "PHYS"))

        // Vérifier que les relations existent
        assertEquals(1, database.foodSpeciesDao().getSpeciesForFood(foodId).size)
        assertEquals(1, database.foodIndicationDao().getIndicationsForFood(foodId).size)

        // Supprimer l'aliment
        database.foodDao().deleteById(foodId)

        // Vérifier que les relations ont été supprimées en cascade
        assertEquals(0, database.foodSpeciesDao().getSpeciesForFood(foodId).size)
        assertEquals(0, database.foodIndicationDao().getIndicationsForFood(foodId).size)
    }

    @Test
    fun testUniqueConstraints() = runBlocking {
        // Tester l'unicité des relations espèces
        val species1 = FoodSpeciesEntity("food-1", "CHIEN")
        val species2 = FoodSpeciesEntity("food-1", "CHIEN") // Même combinaison

        database.foodSpeciesDao().insert(species1)
        database.foodSpeciesDao().insert(species2) // Devrait remplacer species1

        val speciesRelations = database.foodSpeciesDao().getSpeciesForFood("food-1")
        assertEquals(1, speciesRelations.size)

        // Tester l'unicité des relations indications
        val indication1 = FoodIndicationEntity("food-1", "PHYS")
        val indication2 = FoodIndicationEntity("food-1", "PHYS") // Même combinaison

        database.foodIndicationDao().insert(indication1)
        database.foodIndicationDao().insert(indication2) // Devrait remplacer indication1

        val indicationRelations = database.foodIndicationDao().getIndicationsForFood("food-1")
        assertEquals(1, indicationRelations.size)
    }
}


import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoodRelationEntitiesTest {
    private lateinit var database: AndroidDatabase
    private lateinit var alimentDao: AlimentDao

    @Before
    fun createDb() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                AndroidDatabase::class.java
                        )
                        .build()
        alimentDao = database.alimentDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testFoodSpeciesRelation() = runBlocking {
        // Créer des entités de relation espèces
        val species1 = FoodSpeciesEntity("food-1", "CHIEN")
        val species2 = FoodSpeciesEntity("food-1", "CHAT")

        // Insérer via DAO
        database.foodSpeciesDao().insert(species1)
        database.foodSpeciesDao().insert(species2)

        // Récupérer les relations
        val relations = database.foodSpeciesDao().getSpeciesForFood("food-1")
        assertEquals(2, relations.size)
        assertTrue(relations.any { it.species == "CHIEN" })
        assertTrue(relations.any { it.species == "CHAT" })
    }

    @Test
    fun testFoodIndicationRelation() = runBlocking {
        // Créer des entités de relation indications
        val indication1 = FoodIndicationEntity("food-1", "PHYS")
        val indication2 = FoodIndicationEntity("food-1", "SEN")

        // Insérer via DAO
        database.foodIndicationDao().insert(indication1)
        database.foodIndicationDao().insert(indication2)

        // Récupérer les relations
        val relations = database.foodIndicationDao().getIndicationsForFood("food-1")
        assertEquals(2, relations.size)
        assertTrue(relations.any { it.indication == "PHYS" })
        assertTrue(relations.any { it.indication == "SEN" })
    }

    @Test
    fun testCascadeDelete() = runBlocking {
        // Créer un aliment avec des relations
        val foodId = "test-cascade"

        // Ajouter des relations
        database.foodSpeciesDao().insert(FoodSpeciesEntity(foodId, "CHIEN"))
        database.foodIndicationDao().insert(FoodIndicationEntity(foodId, "PHYS"))

        // Vérifier que les relations existent
        assertEquals(1, database.foodSpeciesDao().getSpeciesForFood(foodId).size)
        assertEquals(1, database.foodIndicationDao().getIndicationsForFood(foodId).size)

        // Supprimer l'aliment
        database.foodDao().deleteById(foodId)

        // Vérifier que les relations ont été supprimées en cascade
        assertEquals(0, database.foodSpeciesDao().getSpeciesForFood(foodId).size)
        assertEquals(0, database.foodIndicationDao().getIndicationsForFood(foodId).size)
    }

    @Test
    fun testUniqueConstraints() = runBlocking {
        // Tester l'unicité des relations espèces
        val species1 = FoodSpeciesEntity("food-1", "CHIEN")
        val species2 = FoodSpeciesEntity("food-1", "CHIEN") // Même combinaison

        database.foodSpeciesDao().insert(species1)
        database.foodSpeciesDao().insert(species2) // Devrait remplacer species1

        val speciesRelations = database.foodSpeciesDao().getSpeciesForFood("food-1")
        assertEquals(1, speciesRelations.size)

        // Tester l'unicité des relations indications
        val indication1 = FoodIndicationEntity("food-1", "PHYS")
        val indication2 = FoodIndicationEntity("food-1", "PHYS") // Même combinaison

        database.foodIndicationDao().insert(indication1)
        database.foodIndicationDao().insert(indication2) // Devrait remplacer indication1

        val indicationRelations = database.foodIndicationDao().getIndicationsForFood("food-1")
        assertEquals(1, indicationRelations.size)
    }
}
