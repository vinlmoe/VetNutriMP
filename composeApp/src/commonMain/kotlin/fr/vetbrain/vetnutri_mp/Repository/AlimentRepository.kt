package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

/**
 * Implémentation du repository pour les aliments. Cette classe délègue les opérations à la source
 * de données fournie.
 *
 * @deprecated Cette classe est maintenue pour des raisons de rétrocompatibilité uniquement.
 * Utilisez directement FoodRepository à la place, car les tables ALIMENT_BASE et FOOD ont été
 * fusionnées en une seule table FOOD.
 */
@Deprecated(
        "Utilisez directement FoodRepository, car les tables ALIMENT_BASE et FOOD ont été fusionnées",
        ReplaceWith("FoodRepository")
)
class AlimentRepository(private val dataSource: FoodRepository) {

    suspend fun getAllAliments(): List<AlimentEv> {
        return dataSource.getAllFoods()
    }

    fun observeAllAliments(): Flow<List<AlimentEv>> {
        return dataSource.observeAllFoods()
    }

    suspend fun getAlimentByUUID(uuid: String): AlimentEv? {
        return dataSource.getFood(uuid)
    }

    suspend fun saveAliment(aliment: AlimentEv) {
        println("DEBUG AlimentRepository: Début saveAliment - UUID: ${aliment.uuid}")
        println("DEBUG AlimentRepository: Sauvegarde de l'aliment: ${aliment.nom}")
        println("DEBUG AlimentRepository: Nombre de nutriments: ${aliment.valMap.size}")

        aliment.valMap.forEach { (nutrient, quantity) ->
            println("DEBUG AlimentRepository: Nutriment ${nutrient.label} = ${quantity.value}")
        }

        try {
            val existingFood = dataSource.getFood(aliment.uuid)
            if (existingFood != null) {
                println(
                        "DEBUG AlimentRepository: Aliment existant trouvé, mise à jour: ${existingFood.nom}"
                )
                println("DEBUG AlimentRepository: Appel à updateFood")
                dataSource.updateFood(aliment)
                println("DEBUG AlimentRepository: updateFood terminé avec succès")
            } else {
                println(
                        "DEBUG AlimentRepository: Aliment non trouvé, insertion d'un nouvel aliment"
                )
                println("DEBUG AlimentRepository: Appel à insertFood")
                dataSource.insertFood(aliment)
                println("DEBUG AlimentRepository: insertFood terminé avec succès")
            }
            println("DEBUG AlimentRepository: Sauvegarde terminée avec succès")
        } catch (e: Exception) {
            println("DEBUG AlimentRepository: ERREUR lors de la sauvegarde: ${e.message}")
            e.printStackTrace()
            throw e // Relancer l'exception pour que la vue puisse la gérer
        }
    }

    suspend fun deleteAliment(aliment: AlimentEv) {
        dataSource.deleteFood(aliment.uuid)
    }

    suspend fun importAliments(aliments: List<AlimentEvJson>): FoodImportResult {
        return dataSource.importFoods(aliments)
    }

    companion object {
        private var instance: AlimentRepository? = null
        private var _databaseFoodRepository: FoodRepository? = null

        fun getInstance(foodRepository: FoodRepository): AlimentRepository {
            if (instance == null) {
                instance = AlimentRepository(foodRepository)
            }
            return instance!!
        }

        // Méthode pour initialiser le repository
        fun initializeDatabaseFoodRepository(databaseFoodRepository: FoodRepository) {
            _databaseFoodRepository = databaseFoodRepository
        }

        // Pour les besoins du ViewModel, fournir une méthode statique simplifiée
        fun getAllAliments(): List<AlimentEv> {
            // Utiliser le repository réel si disponible
            return if (_databaseFoodRepository != null) {
                runBlocking { _databaseFoodRepository!!.getAllFoods() }
            } else {
                // Retourner une liste vide si le repository n'est pas initialisé
                emptyList()
            }
        }
    }
}
