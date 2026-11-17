package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.DataBase.MultiRationExplorationDao
import fr.vetbrain.vetnutri_mp.DataBase.MultiRationExplorationEntity
import fr.vetbrain.vetnutri_mp.Data.ExplorationEnregistree
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/** Configurations d'exploration multiration enregistrées (nom, espèce, configuration JSON). */
interface MultiRationExplorationRepository {
    suspend fun getAllExplorations(): List<ExplorationEnregistree>
    suspend fun getExplorationById(uuid: String): ExplorationEnregistree?
    suspend fun saveExploration(exploration: ExplorationEnregistree)
    suspend fun deleteExploration(uuid: String)
}

class DatabaseMultiRationExplorationRepository(private val dao: MultiRationExplorationDao) :
        MultiRationExplorationRepository {

    override suspend fun getAllExplorations(): List<ExplorationEnregistree> =
            withContext(AppDispatchers.IO) { dao.getAll().map { it.toDomain() } }

    override suspend fun getExplorationById(uuid: String): ExplorationEnregistree? =
            withContext(AppDispatchers.IO) { dao.getById(uuid)?.toDomain() }

    override suspend fun saveExploration(exploration: ExplorationEnregistree) {
        withContext(AppDispatchers.IO) {
            dao.upsert(
                    MultiRationExplorationEntity(
                            uuid = exploration.uuid,
                            nom = exploration.nom,
                            espece = exploration.espece,
                            configurationJson = exploration.configurationJson,
                            updatedAt = exploration.updatedAt
                    )
            )
        }
    }

    override suspend fun deleteExploration(uuid: String) {
        withContext(AppDispatchers.IO) { dao.deleteById(uuid) }
    }

    private fun MultiRationExplorationEntity.toDomain() =
            ExplorationEnregistree(
                    uuid = uuid,
                    nom = nom,
                    espece = espece,
                    configurationJson = configurationJson,
                    updatedAt = updatedAt
            )
}

/** Implémentation en mémoire (tests). */
class InMemoryMultiRationExplorationRepository : MultiRationExplorationRepository {
    private val explorations = mutableMapOf<String, ExplorationEnregistree>()

    override suspend fun getAllExplorations(): List<ExplorationEnregistree> =
            explorations.values.sortedByDescending { it.updatedAt }

    override suspend fun getExplorationById(uuid: String): ExplorationEnregistree? = explorations[uuid]

    override suspend fun saveExploration(exploration: ExplorationEnregistree) {
        explorations[exploration.uuid] = exploration
    }

    override suspend fun deleteExploration(uuid: String) {
        explorations.remove(uuid)
    }
}
