package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import kotlinx.coroutines.flow.Flow

/** Interface définissant les opérations de base pour la gestion des aliments. */
interface FoodRepository {
    suspend fun insert(food: AlimentEv)
    suspend fun update(food: AlimentEv)
    suspend fun delete(food: AlimentEv)
    suspend fun getAllFoods(): List<AlimentEv>
    suspend fun getFoodById(id: String): AlimentEv?
    fun observeAllFoods(): Flow<List<AlimentEv>>
}
