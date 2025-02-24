package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implémentation SQLite du FoodRepository pour iOS. Cette classe utilise SQLiter comme driver
 * SQLite.
 */
class SqliteFoodRepository(private val foodDao: FoodDao) : FoodRepository {
    private val foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())

    override suspend fun insert(food: AlimentEv) {
        foodDao.insert(food)
        refreshFoods()
    }

    override suspend fun update(food: AlimentEv) {
        foodDao.update(food)
        refreshFoods()
    }

    override suspend fun delete(food: AlimentEv) {
        foodDao.delete(food)
        refreshFoods()
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return foodDao.getAllFoods()
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foodDao.getFoodById(id)
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return foodsFlow.asStateFlow()
    }

    private suspend fun refreshFoods() {
        foodsFlow.value = getAllFoods()
    }
}
