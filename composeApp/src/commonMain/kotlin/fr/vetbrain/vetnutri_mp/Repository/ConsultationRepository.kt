package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.ConsultationDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toSupplementalVariableEntities
import fr.vetbrain.vetnutri_mp.DataBase.SupplementalVariableEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import kotlinx.coroutines.withContext

interface ConsultationRepository {
    suspend fun saveConsultation(consultation: ConsultationEv)
    suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv>
    suspend fun getConsultationById(id: String): ConsultationEv?
    suspend fun deleteConsultation(consultation: ConsultationEv)
    /** Ajoute les aliments de [recipe] à la ration [rationId] ; retourne le nombre d'aliments non insérés. */
    suspend fun applyRecipeToRation(recipe: Ration, rationId: String): Int
    suspend fun getAllKeywords(): List<ConsultationKeyword>
    suspend fun saveKeyword(keyword: ConsultationKeyword)
}

class DatabaseConsultationRepository(
        private val consultationDao: ConsultationDao,
        private val foodRepository: FoodRepository
) : ConsultationRepository {
    override suspend fun applyRecipeToRation(recipe: Ration, rationId: String): Int {
        return withContext(AppDispatchers.IO) {
            var skipped = 0
            recipe.alimentMutableList.forEach { aliment ->
                try {
                    val entity = aliment.copy(refRation = rationId).toEntity()
                    consultationDao.insertAlimentRation(entity)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    skipped++
                }
            }
            skipped
        }
    }
    override suspend fun saveConsultation(consultation: ConsultationEv) {
        withContext(AppDispatchers.IO) {
            try {
                val existingConsultation = consultationDao.getConsultationById(consultation.uuid)
                val entity = consultation.toEntity()

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
                    ration.toEntity().also { it.idConsult = consultation.uuid }
                }
                val alimentEntities = consultation.rations.flatMap { ration ->
                    ration.alimentMutableList
                        .filter { it.refAlimUnif != null }
                        .map { aliment ->
                            aliment.refRation = ration.uuid
                            aliment.toEntity()
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

                // Variables d'énergie propres à chaque étape de plan évolutif
                val rationSuppVarEntities = consultation.rations.flatMap { ration ->
                    ration.toSupplementalVariableEntities()
                }

                // DELETE + INSERT atomiques : sans transaction, un échec laisserait la
                // consultation sans rations si le DELETE avait déjà eu lieu
                consultationDao.replaceConsultationRelations(
                    consultation.uuid,
                    rationEntities,
                    alimentEntities,
                    suppVarEntities,
                    rationSuppVarEntities
                )
            } catch (e: Exception) {
                throw e
            }
        }
        DatabaseChangeNotifier.notifyChange(
            DatabaseChangeNotifier.ChangeType.CONSULTATION_UPDATED,
            consultation.uuid
        )
    }

    override suspend fun getConsultationsForAnimal(animalId: String): List<ConsultationEv> {
        return withContext(AppDispatchers.IO) {
            val consultations = consultationDao.getConsultationsForAnimal(animalId)

            // Variables et rations de toutes les consultations en une requête par lot
            // (au lieu d'une requête par consultation)
            val consultationIds = consultations.map { it.uuid }
            val suppVarsByConsultation = consultationIds
                .chunked(LOT_IDENTIFIANTS)
                .flatMap { consultationDao.getSupplementalVariablesForConsultations(it) }
                .groupBy { it.idConsult }
            val rationsByConsultation = consultationIds
                .chunked(LOT_IDENTIFIANTS)
                .flatMap { consultationDao.getRationsForConsultations(it) }
                .groupBy { it.idConsult }

            val consultationEvs = consultations.map { consultationEntity ->
                consultationEntity.toData(
                    rations = rationsByConsultation[consultationEntity.uuid] ?: emptyList(),
                    suppVars = suppVarsByConsultation[consultationEntity.uuid] ?: emptyList()
                )
            }
            chargerAlimentsEtVariablesEtape(consultationEvs.flatMap { it.rations })

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

            chargerAlimentsEtVariablesEtape(consultationEv.rations)

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

    /**
     * Charge les aliments et les variables d'énergie propres à chaque étape de plan évolutif
     * pour toutes les rations données, en une requête par lot plutôt qu'une par ration.
     */
    private suspend fun chargerAlimentsEtVariablesEtape(rations: List<Ration>) {
        if (rations.isEmpty()) return
        val rationIds = rations.map { it.uuid }
        val alimentsByRation = rationIds
            .chunked(LOT_IDENTIFIANTS)
            .flatMap { consultationDao.getAlimentsForRations(it) }
            .groupBy { it.refRation }
        val suppVarsByRation = rationIds
            .chunked(LOT_IDENTIFIANTS)
            .flatMap { consultationDao.getSupplementalVariablesForRations(it) }
            .groupBy { it.idRation }
        rations.forEach { ration ->
            val aliments = alimentsByRation[ration.uuid] ?: emptyList()
            ration.alimentMutableList.clear()
            ration.alimentMutableList.addAll(aliments.map { it.toData() })
            ration.suppVarp =
                (suppVarsByRation[ration.uuid] ?: emptyList())
                    .map { it.toData() }
                    .toMutableList()
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
        DatabaseChangeNotifier.notifyChange(
            DatabaseChangeNotifier.ChangeType.CONSULTATION_DELETED,
            consultation.uuid
        )
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
        DatabaseChangeNotifier.notifyDataWritten("Mot-clé de consultation")
    }

    private companion object {
        /** Taille des lots d'identifiants pour les requêtes `IN (...)` (limite SQLite : 999 paramètres). */
        const val LOT_IDENTIFIANTS = 500
    }
}
