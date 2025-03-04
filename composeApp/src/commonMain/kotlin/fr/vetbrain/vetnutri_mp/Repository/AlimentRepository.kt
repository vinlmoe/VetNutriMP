package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import kotlinx.coroutines.flow.Flow

/**
 * Implémentation du repository pour les aliments. Cette classe délègue les opérations à la source
 * de données fournie.
 */
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
        if (dataSource.getFood(aliment.uuid) != null) {
            dataSource.updateFood(aliment)
        } else {
            dataSource.insertFood(aliment)
        }
    }

    suspend fun deleteAliment(aliment: AlimentEv) {
        dataSource.deleteFood(aliment.uuid)
    }

    suspend fun importAliments(aliments: List<AlimentEvJson>): Int {
        return dataSource.importFoods(aliments)
    }
}
