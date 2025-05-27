package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Repository pour la persistance des références évaluées avec Room Multiplatform - Version
 * simplifiée
 */
class DatabaseReferenceEvRepository(
        private val referenceEvDao: ReferenceEvDao,
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) : ReferenceEvRepositoryInterface {

    // Implémentation de l'interface ReferenceEvRepositoryInterface
    override suspend fun getAllReferenceEv(): List<ReferenceEv> {
        val entities = referenceEvDao.getAllReferenceEv()
        return entities.map { entity -> convertEntityToReferenceEv(entity) }
    }

    override suspend fun getReferenceEvById(uuid: String): ReferenceEv? {
        val entity = referenceEvDao.getReferenceEvById(uuid) ?: return null
        return convertEntityToReferenceEv(entity)
    }

    override suspend fun saveReferenceEv(referenceEv: ReferenceEv): String {
        // Sauvegarder l'entité principale seulement pour l'instant
        val entity = convertReferenceEvToEntity(referenceEv)
        referenceEvDao.insertReferenceEv(entity)
        return referenceEv.uuid
    }

    override suspend fun updateReferenceEv(referenceEv: ReferenceEv) {
        val entity = convertReferenceEvToEntity(referenceEv)
        referenceEvDao.updateReferenceEv(entity)
    }

    override suspend fun deleteReferenceEv(uuid: String) {
        referenceEvDao.deleteReferenceEvById(uuid)
    }

    override suspend fun getReferenceEvByEspece(espece: String): List<ReferenceEv> {
        val entities = referenceEvDao.getReferenceEvByEspece(espece)
        return entities.map { entity -> convertEntityToReferenceEv(entity) }
    }

    override fun observeAllReferenceEv(): Flow<List<ReferenceEv>> = flow {
        emit(getAllReferenceEv())
    }

    override fun observeReferenceEvById(uuid: String): Flow<ReferenceEv?> = flow {
        emit(getReferenceEvById(uuid))
    }

    // Méthodes pour compatibilité avec ReferenceEvRepository existant
    fun getAll(): List<ReferenceEv> = runBlocking { getAllReferenceEv() }

    fun getById(id: String): ReferenceEv? = runBlocking { getReferenceEvById(id) }

    fun create(referenceEv: ReferenceEv): Boolean {
        return try {
            runBlocking { saveReferenceEv(referenceEv) }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun update(referenceEv: ReferenceEv): Boolean {
        return try {
            runBlocking { updateReferenceEv(referenceEv) }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun delete(id: String): Boolean {
        return try {
            runBlocking { deleteReferenceEv(id) }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Méthodes privées pour la conversion

    private fun convertEntityToReferenceEv(entity: ReferenceEvEntity): ReferenceEv {
        val referenceEv =
                ReferenceEv(
                        uuid = entity.uuid,
                        nom = entity.nom,
                        description = entity.description,
                        maladie = entity.maladie,
                        nomMaladie = entity.nomMaladie,
                        nomEnergie = entity.nomEnergie,
                        consistent = entity.consistent,
                        espece = Espece.valueOf(entity.espece),
                        stadePhysio = StadePhysio.valueOf(entity.stadePhysio)
                )

        // Assigner les noms des coefficients
        referenceEv.nomk1 = entity.nomk1
        referenceEv.nomk2 = entity.nomk2
        referenceEv.nomk3 = entity.nomk3
        referenceEv.nomk4 = entity.nomk4
        referenceEv.nomk5 = entity.nomk5

        return referenceEv
    }

    private fun convertReferenceEvToEntity(referenceEv: ReferenceEv): ReferenceEvEntity {
        return ReferenceEvEntity(
                uuid = referenceEv.uuid,
                nom = referenceEv.nom,
                description = referenceEv.description,
                maladie = referenceEv.maladie,
                nomMaladie = referenceEv.nomMaladie,
                nomEnergie = referenceEv.nomEnergie,
                consistent = referenceEv.consistent,
                espece = referenceEv.espece.name,
                stadePhysio = referenceEv.stadePhysio.name,
                nomk1 = referenceEv.nomk1,
                nomk2 = referenceEv.nomk2,
                nomk3 = referenceEv.nomk3,
                nomk4 = referenceEv.nomk4,
                nomk5 = referenceEv.nomk5
        )
    }
}
