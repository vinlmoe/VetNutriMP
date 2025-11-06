package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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
                    try {
                        consultationDao.insert(entity)
                    } catch (e: Exception) {
                        // En cas de conflit d'UUID, essayer avec un nouvel UUID
                        if (e.message?.contains("UNIQUE constraint failed") == true) {
                            val newUuid = fr.vetbrain.vetnutri_mp.Utils.genUUID()
                            val newEntity = entity.copy(uuid = newUuid)
                            consultationDao.insert(newEntity)
                            
                            // Mettre à jour l'UUID de la consultation originale
                            consultation.uuid = newUuid
                        } else {
                            throw e
                        }
                    }
                }

                // Sauvegarder les variables supplémentaires
                consultation.suppVarp.forEach { suppVar ->
                    suppVar.variable?.let { variable ->
                        consultationDao.insertSupplementalVariable(
                                SupplementalVariableEntity(
                                        idConsult = consultation.uuid,
                                        variableKind = variable.uuid,
                                        value = suppVar.varue ?: 0.0
                                )
                        )
                    }
                }

                // Sauvegarder les rations
                consultation.rations.forEach { ration ->
                    val rationEntity = ration.toEntity()
                    rationEntity.idConsult = consultation.uuid
                    consultationDao.insertRation(rationEntity)

                    // Sauvegarder les aliments de la ration
                    ration.alimentMutableList.forEach { aliment ->
                        // S'assurer que la référence à la ration est bien définie
                        aliment.refRation = ration.uuid

                        // Vérifier si l'aliment a une référence valide
                        if (aliment.refAlimUnif != null) {
                            try {
                                // Convertir l'AlimentRation en AlimentRationEntity et l'insérer
                                val alimentEntity = aliment.toEntity()
                                consultationDao.insertAlimentRation(alimentEntity)
                            } catch (e: Exception) {}
                        } else {}
                    }
                }
            } catch (e: Exception) {
                // Log de l'erreur pour le débogage
                throw e
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

                // Créer d'abord la consultation avec les entités RationEntity
                val consultation = consultationEntity.toData(rations = rations, suppVars = suppVars)

                // Maintenant, pour chaque ration dans la consultation créée, récupérer et associer
                // les aliments
                consultation.rations.forEach { ration ->
                    // Charger les aliments pour cette ration
                    val aliments = consultationDao.getAlimentsForRation(ration.uuid)

                    // Remplacer la liste d'aliments vide par les aliments chargés
                    ration.alimentMutableList.clear()
                    ration.alimentMutableList.addAll(aliments.map { it.toData() })

                    // Pour chaque AlimentRation, charger les détails complets de l'aliment
                    ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                        alimentRation.refAlimUnif?.let { alimentUuid ->
                            // Charger l'aliment complet depuis le FoodRepository
                            val alimentEv = foodRepository.getFood(alimentUuid)

                            if (alimentEv != null) {
                                // Mettre à jour l'objet AlimentRation avec les détails complets
                                ration.alimentMutableList[index] =
                                        alimentRation.copy(aliment = alimentEv)
                            } else {
                                // Essayer avec getFoodById au cas où
                                val alimentById = foodRepository.getFoodById(alimentUuid)
                                if (alimentById != null) {
                                    ration.alimentMutableList[index] =
                                            alimentRation.copy(aliment = alimentById)
                                } else {}
                            }
                        }
                    }
                }

                consultation
            }
        }
    }

    override suspend fun getConsultationById(id: String): ConsultationEv? {
        return withContext(AppDispatchers.IO) {
            val consultation = consultationDao.getConsultationById(id) ?: return@withContext null
            val suppVars =
                    consultationDao.getSupplementalVariablesForConsultation(consultation.uuid)
            val rations = consultationDao.getRationsForConsultation(consultation.uuid)

            // Créer d'abord la consultation avec les entités RationEntity
            val consultationEv = consultation.toData(rations = rations, suppVars = suppVars)

            // Maintenant, pour chaque ration dans la consultation créée, récupérer et associer les
            // aliments
            consultationEv.rations.forEach { ration ->
                // Charger les aliments pour cette ration
                val aliments = consultationDao.getAlimentsForRation(ration.uuid)

                // Remplacer la liste d'aliments vide par les aliments chargés
                ration.alimentMutableList.clear()
                ration.alimentMutableList.addAll(aliments.map { it.toData() })

                // Pour chaque AlimentRation, charger les détails complets de l'aliment
                ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                    val alimentUuid = alimentRation.refAlimUnif

                    if (alimentUuid != null) {
                        // Charger l'aliment complet depuis le FoodRepository
                        val alimentEv = foodRepository.getFood(alimentUuid)

                        // Mettre à jour l'objet AlimentRation avec les détails complets
                        if (alimentEv != null) {
                            ration.alimentMutableList[index] =
                                    alimentRation.copy(aliment = alimentEv)
                        } else {
                            // Essayer avec getFoodById au cas où
                            val alimentById = foodRepository.getFoodById(alimentUuid)
                            if (alimentById != null) {
                                ration.alimentMutableList[index] =
                                        alimentRation.copy(aliment = alimentById)
                            } else {}
                        }
                    } else {}
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
}
