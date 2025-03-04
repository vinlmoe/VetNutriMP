package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implémentation temporaire du FoodRepository utilisant une liste en mémoire. À remplacer par une
 * implémentation utilisant la base de données locale.
 */
class LocalAlimentDataSource : FoodRepository {
    private val foods = mutableListOf<AlimentEv>()
    private val _foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())

    init {
        // Charger les aliments depuis la base de données locale ou un fichier JSON
    }

    override suspend fun insert(food: AlimentEv) {
        foods.add(food)
        _foodsFlow.value = foods.toList()
    }

    override suspend fun update(food: AlimentEv) {
        val index = foods.indexOfFirst { it.uuid == food.uuid }
        if (index != -1) {
            foods[index] = food
            _foodsFlow.value = foods.toList()
        }
    }

    override suspend fun delete(food: AlimentEv) {
        foods.removeIf { it.uuid == food.uuid }
        _foodsFlow.value = foods.toList()
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return foods.toList()
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foods.find { it.uuid == id }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return _foodsFlow.asStateFlow()
    }

    override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
        // Convertir AlimentEvJson en AlimentEv et les insérer
        // Cette méthode nécessiterait une logique de conversion
        return 0
    }

    override suspend fun insertFood(food: AlimentEv) {
        insert(food)
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return getFoodById(uuid)
    }

    override suspend fun deleteFood(uuid: String) {
        foods.find { it.uuid == uuid }?.let { delete(it) }
    }

    override suspend fun updateFood(food: AlimentEv) {
        update(food)
    }
}
