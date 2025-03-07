package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implémentation SQLite du FoodRepository pour iOS. Cette classe utilise SQLiter comme driver
 * SQLite.
 */
class SqliteFoodRepository : FoodRepository {
    private val foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())
    private val _foods = mutableListOf<AlimentEv>()

    override suspend fun insert(food: AlimentEv) {
        _foods.add(food)
        refreshFoods()
    }

    override suspend fun update(food: AlimentEv) {
        val index = _foods.indexOfFirst { it.uuid == food.uuid }
        if (index != -1) {
            _foods[index] = food
            refreshFoods()
        }
    }

    override suspend fun delete(food: AlimentEv) {
        _foods.removeAll { food -> food.uuid == food.uuid }
        refreshFoods()
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return _foods.toList()
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return _foods.find { it.uuid == id }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return foodsFlow.asStateFlow()
    }

    override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
        var importCount = 0

        foods.forEach { jsonFood ->
            // Créer un AlimentEv à partir de AlimentEvJson
            val alimentEv =
                    AlimentEv(
                            uuid = jsonFood.UUID,
                            nom = jsonFood.nom ?: "",
                            typeAliment = null, // À personnaliser selon les besoins
                            group = null, // À personnaliser selon les besoins
                            consistent = true,
                            deprecated = jsonFood.deprecated ?: false,
                            price = jsonFood.prix,
                            categPrice = jsonFood.categoriePrix,
                            ingredients = jsonFood.ingredients,
                            brand = jsonFood.marque,
                            gamme = jsonFood.gamme,
                            quantInt = jsonFood.quantInt,
                            dataB = jsonFood.DataB,
                            especes = jsonFood.Especes.toMutableList(),
                            indicat = mutableListOf(), // À personnaliser selon les besoins
                            valMap = mutableMapOf(), // À personnaliser selon les besoins
                            rationUUID = "" // Valeur par défaut pour rationUUID
                    )

            // Ajouter à la liste
            _foods.add(alimentEv)
            importCount++
        }

        refreshFoods()
        return importCount
    }

    override suspend fun insertFood(food: AlimentEv) {
        insert(food)
    }

    override suspend fun updateFood(food: AlimentEv) {
        update(food)
    }

    override suspend fun deleteFood(uuid: String) {
        _foods.removeAll { food -> food.uuid == uuid }
        refreshFoods()
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return getFoodById(uuid)
    }

    private suspend fun refreshFoods() {
        foodsFlow.value = _foods.toList()
    }
}
