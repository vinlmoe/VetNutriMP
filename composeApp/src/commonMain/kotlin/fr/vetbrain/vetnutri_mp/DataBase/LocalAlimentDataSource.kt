package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import fr.vetbrain.vetnutri_mp.Data.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEvLight
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
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
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        // Initialiser le flow au démarrage
        coroutineScope.launch { refreshFoodsFlow() }
    }

    override suspend fun insert(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.insertFood(foodEntity)
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

    /**
     * Récupère une liste légère de tous les aliments sans les valeurs nutritionnelles. Cette
     * méthode est optimisée pour les performances lorsque seules les informations de base des
     * aliments sont nécessaires.
     *
     * @return Une liste d'objets AlimentEvLight contenant les informations de base des aliments
     */
    override suspend fun getAllFoodsLight(): List<AlimentEvLight> {
        return foodDao.findAll().map { it.toAlimentEvLight() }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return foodDao.getFoodById(id)?.toAlimentEv()
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        // Pas besoin d'appeler refreshFoodsFlow() ici, car c'est une fonction suspend
        // et nous sommes dans une fonction non-suspend
        return _foodsFlow.asStateFlow()
    }

    override suspend fun importFoods(
            foods: List<AlimentEvJson>
    ): fr.vetbrain.vetnutri_mp.Repository.FoodImportResult {
        var count = 0
        var updated = 0
        var imported = 0
        var errors = 0

        foods.forEach { foodJson ->
            try {
                // Déboguer la valMap avant conversion

                if (foodJson.valMap.isNotEmpty()) {
                    foodJson.valMap.entries.take(5).forEach { (key, value) -> }
                } else {}

                // Utiliser la fonction d'extension toData() définie dans JsonMappers.kt
                val alimentEv = foodJson.toData()

                // Déboguer la carte des nutriments après conversion
                if (alimentEv.valMap.isNotEmpty()) {
                    alimentEv.valMap.entries.take(5).forEach { (nutrient, value) -> }
                } else {}

                // Vérifier si l'aliment existe déjà
                val existingFood = getFood(alimentEv.uuid)
                if (existingFood != null) {
                    // Mettre à jour l'aliment existant
                    updateFood(alimentEv)
                    updated++
                } else {
                    // Insérer un nouvel aliment
                    insertFood(alimentEv)
                    imported++
                }
                count++
            } catch (e: Exception) {
                e.printStackTrace()
                errors++
            }
        }

        refreshFoodsFlow()

        val totalFoods = getAllFoods().size

        return fr.vetbrain.vetnutri_mp.Repository.FoodImportResult(
                importedCount = imported,
                updatedCount = updated,
                deletedCount = 0,
                errorCount = errors,
                totalCount = totalFoods,
                nonResolvedNutrientsCount = 0
        )
    }

    override suspend fun insertFood(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.insertFood(foodEntity)

        // Ajouter les valeurs de nutriments
        if (food.valMap.isNotEmpty()) {
            val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)
            nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
            nutrientValueDao.insertNutrientValues(nutrientValues)
        }

        refreshFoodsFlow()
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        val foodEntity = foodDao.getFood(uuid) ?: return null
        val alimentEv = foodEntity.toAlimentEv()

        // Charger les valeurs des nutriments
        val nutrientValues = nutrientValueDao.getNutrientValues(uuid)
        if (nutrientValues.isNotEmpty()) {
            // Convertir les valeurs des nutriments en Map<Nutrient, NutrientQuantity>
            val nutrientMap =
                    nutrientValues
                            .associate { entity ->
                                val nutrient =
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
                                                .AllNutrientResolver(entity.nutrientLabel)
                                nutrient to
                                        fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                                                entity.value,
                                                entity.nutrientLabel
                                        )
                            }
                            .filterKeys { it != null }
                            .mapKeys { it.key!! }

            // Mettre à jour valMap avec les valeurs chargées
            alimentEv.valMap = nutrientMap.toMutableMap()
        }

        return alimentEv
    }

    override suspend fun deleteFood(uuid: String) {
        foodDao.deleteFood(uuid)
        refreshFoodsFlow()
    }

    override suspend fun updateFood(food: AlimentEv) {
        val foodEntity = food.toFoodEntity()
        foodDao.updateFood(foodEntity)

        // Mettre à jour les valeurs de nutriments
        if (food.valMap.isNotEmpty()) {
            val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)
            nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
            nutrientValueDao.insertNutrientValues(nutrientValues)
        }

        refreshFoodsFlow()
    }

    private suspend fun refreshFoodsFlow() {
        _foodsFlow.value = getAllFoods()
    }

    /** Convertit une map de nutriments et valeurs en liste d'entités de valeurs de nutriments. */
    private fun Map<
            Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>.toNutrientValueEntities(
            alimentUuid: String
    ): List<NutrientValueEntity> {
        return map { (nutrient, nutrientQuantity) ->
            NutrientValueEntity(
                    refAliment = alimentUuid,
                    nutrientLabel = nutrient.label,
                    value = nutrientQuantity.value
            )
        }
    }

    /**
     * Supprime tous les aliments de la base de données, ainsi que leurs valeurs nutritionnelles
     * associées.
     * @return Le nombre d'aliments supprimés
     */
    override suspend fun clearAllFoods(): Int {
        val allFoods = foodDao.getAllFoods()
        val count = allFoods.size

        // Supprimer d'abord toutes les valeurs nutritionnelles pour tous les aliments
        allFoods.forEach { food -> nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid) }

        // Supprimer tous les aliments
        foodDao.deleteAllFoods()

        refreshFoodsFlow()

        return count
    }
}
