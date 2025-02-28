package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.RationDao
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

interface RationRepository {
    suspend fun saveRation(ration: Ration)
    suspend fun getRationsForConsultation(consultationId: String): List<Ration>
    suspend fun getRationById(id: String): Ration?
    suspend fun deleteRation(ration: Ration)
    suspend fun getAllRations(): List<Ration>
}

class DatabaseRationRepository(private val rationDao: RationDao) : RationRepository {
    override suspend fun saveRation(ration: Ration) {
        withContext(AppDispatchers.Default) {
            val entity = ration.toEntity()
            if (getRationById(ration.uuid) != null) {
                rationDao.update(entity)
            } else {
                rationDao.insert(entity)
            }
        }
    }

    override suspend fun getRationsForConsultation(consultationId: String): List<Ration> {
        return withContext(AppDispatchers.Default) {
            rationDao.getRationsForConsultation(consultationId).map { it.toData() }
        }
    }

    override suspend fun getRationById(id: String): Ration? {
        return withContext(AppDispatchers.Default) { rationDao.getRationById(id)?.toData() }
    }

    override suspend fun deleteRation(ration: Ration) {
        withContext(AppDispatchers.Default) { rationDao.delete(ration.toEntity()) }
    }

    override suspend fun getAllRations(): List<Ration> {
        return withContext(AppDispatchers.Default) {
            rationDao.getAllRations().map { it.toData() }
        }
    }
}
