package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.ConsultationDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.SupplementalVariableEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

interface ConsultationRepository {
    suspend fun saveConsultation(consultation: ConsultationEv)
    suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv>
    suspend fun getConsultationById(id: String): ConsultationEv?
    suspend fun deleteConsultation(consultation: ConsultationEv)
    suspend fun applyRecipeToRation(recipe: Ration, rationId: String)
    suspend fun getAllKeywords(): List<ConsultationKeyword>
    suspend fun saveKeyword(keyword: ConsultationKeyword)
}

class DatabaseConsultationRepository(
        private val consultationDao: ConsultationDao,
        private val foodRepository: FoodRepository
) : ConsultationRepository {
    override suspend fun applyRecipeToRation(recipe: Ration, rationId: String) {
        withContext(AppDispatchers.IO) {
            recipe.alimentMutableList.forEach { aliment ->
                try {
                    val entity = aliment.copy(refRation = rationId).toEntity()
                    consultationDao.insertAlimentRation(entity)
                } catch (_: Exception) {}
            }
        }
    }
    override suspend fun saveConsultation(consultation: ConsultationEv) {
        withContext(AppDispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val existingConsultation = consultationDao.getConsultationById(consultation.uuid)
                val entity = consultation.toEntity().copy(updatedAtMs = now)

                if (existingConsultation == null) {
                    try {
                        consultationDao.insert(entity)
                    } catch (e: Exception) {
                        if (e.message?.contains("UNIQUE constraint failed") == true) {
                            val newUuid = fr.vetbrain.vetnutri_mp.Utils.genUUID()
                            consultationDao.insert(entity.copy(uuid = newUuid))
                            consultation.uuid = newUuid
                        } else {
                            throw e
                        }
                    }
                } else {
                    consultationDao.update(entity)
                }

                // Pré-construire toutes les entités avant la transaction
                val rationEntities = consultation.rations.map { ration ->
                    ration.toEntity().copy(updatedAtMs = now).also { it.idConsult = consultation.uuid }
                }
                val alimentEntities = consultation.rations.flatMap { ration ->
                    ration.alimentMutableList
                        .filter { it.refAlimUnif != null }
                        .map { aliment ->
                            aliment.refRation = ration.uuid
                            aliment.toEntity().copy(updatedAtMs = now)
                        }
                }
                val suppVarEntities = consultation.suppVarp.mapNotNull { suppVar ->
                    suppVar.variable?.let { variable ->
                        SupplementalVariableEntity(
                            idConsult = consultation.uuid,
                            variableKind = variable.uuid,
                            value = suppVar.varue ?: 0.0
                        )
                    }
                }

                // DELETE + INSERT atomiques : sans transaction, un échec laisserait la
                // consultation sans rations si le DELETE avait déjà eu lieu
                consultationDao.replaceConsultationRelations(
                    consultation.uuid,
                    rationEntities,
                    alimentEntities,
                    suppVarEntities
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv> {
        return withContext(AppDispatchers.IO) {
            val consultations = consultationDao.getConsultationsForAnimal(animalId)

            val consultationEvs = consultations.map { consultationEntity ->
                val suppVars = consultationDao.getSupplementalVariablesForConsultation(consultationEntity.uuid)
                val rations = consultationDao.getRationsForConsultation(consultationEntity.uuid)
                val consultation = consultationEntity.toData(rations = rations, suppVars = suppVars)

                consultation.rations.forEach { ration ->
                    val aliments = consultationDao.getAlimentsForRation(ration.uuid)
                    ration.alimentMutableList.clear()
                    ration.alimentMutableList.addAll(aliments.map { it.toData() })
                }
                consultation
            }

            // Batch-load tous les aliments référencés en une seule passe
            val allAlimentUuids = consultationEvs
                .flatMap { it.rations }
                .flatMap { it.alimentMutableList }
                .mapNotNull { it.refAlimUnif }
                .distinct()
            val alimentsById = foodRepository.getFoodsByUuids(allAlimentUuids)

            consultationEvs.forEach { consultation ->
                consultation.rations.forEach { ration ->
                    ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                        val alimentEv = alimentRation.refAlimUnif?.let { alimentsById[it] }
                        if (alimentEv != null) {
                            ration.alimentMutableList[index] = alimentRation.copy(aliment = alimentEv)
                        }
                    }
                }
            }

            consultationEvs
        }
    }

    override suspend fun getConsultationById(id: String): ConsultationEv? {
        return withContext(AppDispatchers.IO) {
            val consultation = consultationDao.getConsultationById(id) ?: return@withContext null
            val suppVars = consultationDao.getSupplementalVariablesForConsultation(consultation.uuid)
            val rations = consultationDao.getRationsForConsultation(consultation.uuid)
            val consultationEv = consultation.toData(rations = rations, suppVars = suppVars)

            consultationEv.rations.forEach { ration ->
                val aliments = consultationDao.getAlimentsForRation(ration.uuid)
                ration.alimentMutableList.clear()
                ration.alimentMutableList.addAll(aliments.map { it.toData() })
            }

            // Batch-load tous les aliments référencés en une seule passe
            val allAlimentUuids = consultationEv.rations
                .flatMap { it.alimentMutableList }
                .mapNotNull { it.refAlimUnif }
                .distinct()
            val alimentsById = foodRepository.getFoodsByUuids(allAlimentUuids)

            consultationEv.rations.forEach { ration ->
                ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                    val alimentEv = alimentRation.refAlimUnif?.let { alimentsById[it] }
                    if (alimentEv != null) {
                        ration.alimentMutableList[index] = alimentRation.copy(aliment = alimentEv)
                    }
                }
            }

            consultationEv
        }
    }

    override suspend fun deleteConsultation(consultation: ConsultationEv) {
        withContext(AppDispatchers.IO) {
            // Supprimer d'abord les rations et variables liées
            consultationDao.deleteRationsForConsultation(consultation.uuid)
            consultationDao.deleteSupplementalVariablesForConsultation(consultation.uuid)

            // Puis supprimer la consultation elle-même
            val entity = consultation.toEntity()
            consultationDao.delete(entity)
        }
    }

    override suspend fun getAllKeywords(): List<ConsultationKeyword> {
        return withContext(AppDispatchers.IO) {
            consultationDao.getAllConsultationKeywords().map { it.toData() }
        }
    }

    override suspend fun saveKeyword(keyword: ConsultationKeyword) {
        withContext(AppDispatchers.IO) {
            consultationDao.insertConsultationKeyword(keyword.toEntity())
        }
    }
}
