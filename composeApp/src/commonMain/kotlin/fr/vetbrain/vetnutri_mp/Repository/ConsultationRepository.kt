package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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
}

class DatabaseConsultationRepository(
        private val consultationDao: ConsultationDao,
        private val foodRepository: FoodRepository
) : ConsultationRepository {
    override suspend fun saveConsultation(consultation: ConsultationEv) {
        withContext(AppDispatchers.IO) {
            // Vérifier si la consultation existe déjà
            val existingConsultation = consultationDao.getConsultationById(consultation.uuid)
            val entity = consultation.toEntity()

            if (existingConsultation != null) {
                // Mise à jour de la consultation existante
                consultationDao.update(entity)

                // Supprimer les anciennes rations et variables pour éviter les doublons
                consultationDao.deleteRationsForConsultation(consultation.uuid)
                consultationDao.deleteSupplementalVariablesForConsultation(consultation.uuid)
            } else {
                // Insertion d'une nouvelle consultation
                consultationDao.insert(entity)
            }

            // Sauvegarder les variables supplémentaires
            consultation.suppVarp.forEach { suppVar ->
                suppVar.variable?.let { variable ->
                    consultationDao.insertSupplementalVariable(
                            SupplementalVariableEntity(
                                    idConsult = consultation.uuid,
                                    variableKind = variable.uuid,
                                    value = suppVar.varue
                            )
                    )
                }
            }

            // Sauvegarder les rations
            consultation.rations.forEach { ration ->
                val rationEntity = ration.toEntity()
                rationEntity.idConsult = consultation.uuid
                consultationDao.insertRation(rationEntity)
            }
        }
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv> {
        return withContext(AppDispatchers.IO) {
            val consultations = consultationDao.getConsultationsForAnimal(animalId)
            consultations.map { consultationEntity ->
                val suppVars =
                        consultationDao.getSupplementalVariablesForConsultation(
                                consultationEntity.uuid
                        )
                val rations = consultationDao.getRationsForConsultation(consultationEntity.uuid)

                // Pour chaque ration, récupérer les aliments associés
                val rationsWithAliments =
                        rations.map { rationEntity ->
                            // Récupérer les entités AlimentRation pour cette ration
                            val alimentRationEntities =
                                    consultationDao.getAlimentsForRation(rationEntity.uuid)

                            // Convertir la ration et ses aliments en objets de domaine
                            val ration = rationEntity.toData(alimentRationEntities)

                            // Pour chaque AlimentRation, charger les détails complets de l'aliment
                            ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                                alimentRation.refAlimUnif?.let { alimentUuid ->
                                    // Charger l'aliment complet depuis le FoodRepository
                                    val alimentEv = foodRepository.getFood(alimentUuid)

                                    // Mettre à jour l'objet AlimentRation avec les détails complets
                                    if (alimentEv != null) {
                                        ration.alimentMutableList[index] =
                                                alimentRation.copy(aliment = alimentEv)
                                    }
                                }
                            }

                            ration
                        }

                // Construire la consultation avec ses rations complètes
                consultationEntity.toData(rations = rations, suppVars = suppVars)
            }
        }
    }

    override suspend fun getConsultationById(id: String): ConsultationEv? {
        return withContext(AppDispatchers.IO) {
            val consultation = consultationDao.getConsultationById(id) ?: return@withContext null
            val suppVars =
                    consultationDao.getSupplementalVariablesForConsultation(consultation.uuid)
            val rations = consultationDao.getRationsForConsultation(consultation.uuid)

            // Pour chaque ration, récupérer les aliments associés
            val rationsWithAliments =
                    rations.map { rationEntity ->
                        // Récupérer les entités AlimentRation pour cette ration
                        val alimentRationEntities =
                                consultationDao.getAlimentsForRation(rationEntity.uuid)

                        // Convertir la ration et ses aliments en objets de domaine
                        val ration = rationEntity.toData(alimentRationEntities)

                        // Pour chaque AlimentRation, charger les détails complets de l'aliment
                        ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                            alimentRation.refAlimUnif?.let { alimentUuid ->
                                // Charger l'aliment complet depuis le FoodRepository
                                val alimentEv = foodRepository.getFood(alimentUuid)

                                // Mettre à jour l'objet AlimentRation avec les détails complets
                                if (alimentEv != null) {
                                    ration.alimentMutableList[index] =
                                            alimentRation.copy(aliment = alimentEv)
                                }
                            }
                        }

                        rationEntity
                    }

            // Construire la consultation avec ses rations complètes
            consultation.toData(rations = rations, suppVars = suppVars)
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
}
