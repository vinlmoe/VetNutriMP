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
                            println(
                                    "Inséré: AlimentRation avec ID=${aliment.uuid}, référençant l'aliment ${aliment.refAlimUnif}"
                            )
                        } catch (e: Exception) {
                            println(
                                    "Erreur lors de l'insertion de l'AlimentRation avec ID=${aliment.uuid}: ${e.message}"
                            )
                        }
                    } else {
                        println(
                                "Ignoré: AlimentRation avec ID=${aliment.uuid} n'a pas de référence d'aliment"
                        )
                    }
                }
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

                println(
                        "DEBUG ConsultationRepo - Consultation ${consultationEntity.uuid} chargée avec ${rations.size} rations"
                )

                // Créer d'abord la consultation avec les entités RationEntity
                val consultation = consultationEntity.toData(rations = rations, suppVars = suppVars)

                // Maintenant, pour chaque ration dans la consultation créée, récupérer et associer
                // les aliments
                consultation.rations.forEach { ration ->
                    // Charger les aliments pour cette ration
                    val aliments = consultationDao.getAlimentsForRation(ration.uuid)
                    println(
                            "DEBUG ConsultationRepo - Ration ${ration.name} a ${aliments.size} aliments"
                    )

                    // Remplacer la liste d'aliments vide par les aliments chargés
                    ration.alimentMutableList.clear()
                    ration.alimentMutableList.addAll(aliments.map { it.toData() })

                    println(
                            "DEBUG ConsultationRepo - Chargement des détails pour la ration ${ration.name} (${ration.alimentMutableList.size} aliments)"
                    )

                    // Pour chaque AlimentRation, charger les détails complets de l'aliment
                    ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                        alimentRation.refAlimUnif?.let { alimentUuid ->
                            // Charger l'aliment complet depuis le FoodRepository
                            val alimentEv = foodRepository.getFood(alimentUuid)

                            if (alimentEv != null) {
                                println(
                                        "DEBUG ConsultationRepo - Aliment trouvé et associé: ${alimentEv.nom}"
                                )
                                // Mettre à jour l'objet AlimentRation avec les détails complets
                                ration.alimentMutableList[index] =
                                        alimentRation.copy(aliment = alimentEv)
                            } else {
                                // Essayer avec getFoodById au cas où
                                val alimentById = foodRepository.getFoodById(alimentUuid)
                                if (alimentById != null) {
                                    println(
                                            "DEBUG ConsultationRepo - Aliment trouvé par ID et associé: ${alimentById.nom}"
                                    )
                                    ration.alimentMutableList[index] =
                                            alimentRation.copy(aliment = alimentById)
                                } else {
                                    println(
                                            "DEBUG ConsultationRepo - ERREUR: Impossible de trouver l'aliment avec UUID=$alimentUuid"
                                    )
                                }
                            }
                        }
                                ?: println(
                                        "DEBUG ConsultationRepo - ATTENTION: refAlimUnif est null pour l'AlimentRation ${alimentRation.uuid}"
                                )
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

            println(
                    "DEBUG ConsultationRepo - Consultation ${consultation.uuid} chargée avec ${rations.size} rations"
            )

            // Créer d'abord la consultation avec les entités RationEntity
            val consultationEv = consultation.toData(rations = rations, suppVars = suppVars)

            // Maintenant, pour chaque ration dans la consultation créée, récupérer et associer les
            // aliments
            consultationEv.rations.forEach { ration ->
                // Charger les aliments pour cette ration
                val aliments = consultationDao.getAlimentsForRation(ration.uuid)
                println(
                        "DEBUG ConsultationRepo - Ration ${ration.name} a ${aliments.size} aliments"
                )

                // Remplacer la liste d'aliments vide par les aliments chargés
                ration.alimentMutableList.clear()
                ration.alimentMutableList.addAll(aliments.map { it.toData() })

                println(
                        "DEBUG ConsultationRepo - Chargement des détails pour la ration ${ration.name} (${ration.alimentMutableList.size} aliments)"
                )

                // Pour chaque AlimentRation, charger les détails complets de l'aliment
                ration.alimentMutableList.forEachIndexed { index, alimentRation ->
                    val alimentUuid = alimentRation.refAlimUnif
                    println(
                            "DEBUG ConsultationRepo - AlimentRation[$index]: UUID=${alimentRation.uuid}, refAlimUnif=$alimentUuid"
                    )

                    if (alimentUuid != null) {
                        // Charger l'aliment complet depuis le FoodRepository
                        val alimentEv = foodRepository.getFood(alimentUuid)
                        println(
                                "DEBUG ConsultationRepo - Aliment trouvé: ${alimentEv?.nom ?: "null"}"
                        )

                        // Mettre à jour l'objet AlimentRation avec les détails complets
                        if (alimentEv != null) {
                            ration.alimentMutableList[index] =
                                    alimentRation.copy(aliment = alimentEv)
                            println(
                                    "DEBUG ConsultationRepo - Aliment associé à la ration: ${alimentEv.nom}"
                            )
                        } else {
                            // Essayer avec getFoodById au cas où
                            val alimentById = foodRepository.getFoodById(alimentUuid)
                            if (alimentById != null) {
                                ration.alimentMutableList[index] =
                                        alimentRation.copy(aliment = alimentById)
                                println(
                                        "DEBUG ConsultationRepo - Aliment trouvé par ID et associé: ${alimentById.nom}"
                                )
                            } else {
                                println(
                                        "DEBUG ConsultationRepo - ERREUR: Impossible de trouver l'aliment avec UUID=$alimentUuid"
                                )
                            }
                        }
                    } else {
                        println(
                                "DEBUG ConsultationRepo - ATTENTION: refAlimUnif est null pour l'AlimentRation ${alimentRation.uuid}"
                        )
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
}
