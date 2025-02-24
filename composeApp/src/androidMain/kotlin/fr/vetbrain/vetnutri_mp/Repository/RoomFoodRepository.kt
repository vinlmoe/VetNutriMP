package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import kotlinx.coroutines.flow.Flow

/**
 * Implémentation Room du FoodRepository. Cette classe utilise Room pour gérer la persistance des
 * données.
 */
class RoomFoodRepository(private val foodDao: FoodDao) : FoodRepository {
    override suspend fun insert(food: AlimentEv) {
        foodDao.insert(food)
    }

    override suspend fun update(food: AlimentEv) {
        foodDao.update(food)
    }

    override suspend fun delete(food: AlimentEv) {
        foodDao.delete(food)
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return foodDao.getAllFoods()
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foodDao.getFoodById(id)
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return foodDao.observeAllFoods()
    }
}
