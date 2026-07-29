package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import kotlinx.coroutines.flow.Flow

/** Interface pour le repository des références évaluées */
interface ReferenceEvRepositoryInterface {
    suspend fun getAllReferenceEv(): List<ReferenceEv>
    suspend fun getReferenceEvById(uuid: String): ReferenceEv?
    suspend fun saveReferenceEv(referenceEv: ReferenceEv): String
    suspend fun updateReferenceEv(referenceEv: ReferenceEv)
    suspend fun deleteReferenceEv(uuid: String)
    suspend fun getReferenceEvByEspece(espece: String): List<ReferenceEv>
    fun observeAllReferenceEv(): Flow<List<ReferenceEv>>
    fun observeReferenceEvById(uuid: String): Flow<ReferenceEv?>
}
