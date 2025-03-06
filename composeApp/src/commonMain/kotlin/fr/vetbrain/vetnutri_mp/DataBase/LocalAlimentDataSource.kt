package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Implémentation du FoodRepository utilisant Room Database. */
class LocalAlimentDataSource(
        private val foodDao: FoodDao,
        private val nutrientValueDao: NutrientValueDao
) : FoodRepository {
    private val _foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialiser le flow au démarrage
        coroutineScope.launch { refreshFoodsFlow() }
    }

    override suspend fun insert(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.insert(foodEntity)
        refreshFoodsFlow()
    }

    override suspend fun update(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.update(foodEntity)
        refreshFoodsFlow()
    }

    override suspend fun delete(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.delete(foodEntity)
        refreshFoodsFlow()
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return foodDao.findAll().map { it.toAlimentEv() }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foodDao.getFoodById(id)?.toAlimentEv()
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        // Pas besoin d'appeler refreshFoodsFlow() ici, car c'est une fonction suspend
        // et nous sommes dans une fonction non-suspend
        return _foodsFlow.asStateFlow()
    }

    override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
        var count = 0
        foods.forEach { foodJson ->
            // Utiliser la fonction d'extension toData() définie dans JsonMappers.kt
            val alimentEv = foodJson.toData()
            insertFood(alimentEv)
            count++
        }
        return count
    }

    override suspend fun insertFood(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.insertFood(foodEntity)
        refreshFoodsFlow()
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return foodDao.getFood(uuid)?.toAlimentEv()
    }

    override suspend fun deleteFood(uuid: String) {
        foodDao.deleteFood(uuid)
        refreshFoodsFlow()
    }

    override suspend fun updateFood(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.updateFood(foodEntity)
        refreshFoodsFlow()
    }

    private suspend fun refreshFoodsFlow() {
        _foodsFlow.value = getAllFoods()
    }
}
