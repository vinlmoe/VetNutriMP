package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    /**
     * Récupère une liste légère de tous les aliments sans les valeurs nutritionnelles. Cette
     * méthode est optimisée pour les performances lorsque seules les informations de base des
     * aliments sont nécessaires.
     *
     * @return Une liste d'objets AlimentEvLight contenant les informations de base des aliments
     */
    suspend fun getAllAlimentsLight(): List<AlimentEvLight> {
        return dataSource.getAllFoodsLight()
    }

    fun observeAllAliments(): Flow<List<AlimentEv>> {
        return dataSource.observeAllFoods()
    }

    suspend fun getAlimentByUUID(uuid: String): AlimentEv? {
        return dataSource.getFood(uuid)
    }

    suspend fun saveAliment(aliment: AlimentEv) {

        aliment.valMap.forEach { (nutrient, quantity) ->
        }

        try {
            val existingFood = dataSource.getFood(aliment.uuid)
            if (existingFood != null) {
                dataSource.updateFood(aliment)
            } else {
                dataSource.insertFood(aliment)
            }
        } catch (e: Exception) {
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
                // Utiliser le même repository que les méthodes statiques si disponible
                val repositoryToUse = _databaseFoodRepository ?: foodRepository
                instance = AlimentRepository(repositoryToUse)
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

        /**
         * Récupère une liste légère de tous les aliments sans les valeurs nutritionnelles. Cette
         * méthode est optimisée pour les performances lorsque seules les informations de base des
         * aliments sont nécessaires.
         *
         * @return Une liste d'objets AlimentEvLight contenant les informations de base des aliments
         */
        fun getAllAlimentsLight(): List<AlimentEvLight> {
            // Utiliser le repository réel si disponible
            return if (_databaseFoodRepository != null) {
                runBlocking { _databaseFoodRepository!!.getAllFoodsLight() }
            } else {
                // Retourner une liste vide si le repository n'est pas initialisé
                emptyList()
            }
        }

        /**
         * Récupère un aliment par son UUID. Cette méthode est une version statique simplifiée pour
         * les besoins du ViewModel.
         *
         * @param uuid UUID de l'aliment à récupérer
         * @return L'aliment correspondant ou null si non trouvé ou si le repository n'est pas
         * initialisé
         */
        fun getAlimentByUUID(uuid: String): AlimentEv? {
            // Utiliser le repository réel si disponible
            return if (_databaseFoodRepository != null) {
                runBlocking { _databaseFoodRepository!!.getFood(uuid) }
            } else {
                // Retourner null si le repository n'est pas initialisé
                null
            }
        }

        /**
         * Observe les aliments avec des mises à jour automatiques. Cette méthode est une version
         * statique pour les besoins du ViewModel.
         *
         * @return Flow qui émet la liste des aliments à chaque mise à jour
         */
        fun observeAllAliments(): Flow<List<AlimentEv>> {
            // Utiliser le repository réel si disponible
            return if (_databaseFoodRepository != null) {
                _databaseFoodRepository!!.observeAllFoods()
            } else {
                // Retourner un flow vide si le repository n'est pas initialisé
                flowOf(emptyList())
            }
        }
    }
}
