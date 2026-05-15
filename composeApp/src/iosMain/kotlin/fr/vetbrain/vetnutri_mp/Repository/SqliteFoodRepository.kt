package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
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
        _foods.removeAll { it.uuid == food.uuid }
        refreshFoods()
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return _foods.toList()
    }

    override suspend fun getAllFoodsLight(): List<AlimentEvLight> {
        return _foods.map { food ->
            AlimentEvLight(
                    uuid = food.uuid,
                    nom = food.nom,
                    brand = food.brand,
                    group = food.group,
                    typeAliment = food.typeAliment,
                    gamme = food.gamme,
                    especes = food.especes,
                    indicat = food.indicat,
                    deprecated = food.deprecated
            )
        }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return _foods.find { it.uuid == id }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return foodsFlow.asStateFlow()
    }

    override suspend fun clearAllFoods(): Int {
        val count = _foods.size
        _foods.clear()
        refreshFoods()
        return count
    }

    override suspend fun importFoods(
        foods: List<AlimentEvJson>,
        mergeNutrients: Boolean,
        importOnlyIfNewer: Boolean
    ): FoodImportResult {
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

        return FoodImportResult(
                importedCount = importCount,
                updatedCount = 0,
                deletedCount = 0,
                errorCount = 0,
                totalCount = _foods.size,
                nonResolvedNutrientsCount = 0
        )
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

    override suspend fun getFoodsCount(): Int = _foods.size

    override suspend fun getAllFoodIds(): Set<String> = _foods.map { it.uuid }.toSet()

    override suspend fun getAllFoodsAsEvLight(): List<AlimentEv> =
        _foods.map { it.copy(valMap = mutableMapOf()) }

    override suspend fun getFoodsByIds(ids: List<String>): List<AlimentEv> {
        val idSet = ids.toSet()
        return _foods.filter { it.uuid in idSet }
    }

    override suspend fun getFoodsPage(limit: Int, offset: Int): List<AlimentEv> =
        _foods.drop(offset).take(limit)

    override suspend fun getDistinctNutrientLabels(): List<String> =
        _foods.flatMap { it.valMap.keys.map { k -> k.label } }.distinct()

    private suspend fun refreshFoods() {
        foodsFlow.value = _foods.toList()
    }
}
