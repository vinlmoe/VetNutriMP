package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.CoefficientEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.BiblioRefMappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.BiblioRefMappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.ReferenceEvMappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.ReferenceEvMappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.NutrientReferenceEntity
import fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvDao
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer les références nutritionnelles (ReferenceEv) et leurs relations
 * (nutriments, coefficients).
 */
interface ReferenceEvRepository {
    /**
     * Récupère toutes les références nutritionnelles
     * @return Un Flow contenant la liste des références
     */
    fun getAllReferences(): Flow<List<ReferenceEv>>

    /**
     * Récupère une référence nutritionnelle par son identifiant
     * @param referenceId L'identifiant de la référence
     * @return La référence ou null si non trouvée
     */
    suspend fun getReferenceById(referenceId: String): ReferenceEv?

    /**
     * Récupère les références correspondant à une espèce et un stade physiologique
     * @param espece L'espèce concernée (nom de l'énumération)
     * @param stade Le stade physiologique concerné (nom de l'énumération)
     * @return La liste des références correspondantes
     */
    suspend fun getReferencesByEspeceAndStade(espece: String, stade: String): List<ReferenceEv>

    /**
     * Insère une référence nutritionnelle et toutes ses relations
     * @param reference La référence à insérer
     * @return true si l'insertion a réussi, false sinon
     */
    suspend fun insertReference(reference: ReferenceEv): Boolean

    /**
     * Met à jour une référence nutritionnelle et toutes ses relations
     * @param reference La référence à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    suspend fun updateReference(reference: ReferenceEv): Boolean

    /**
     * Supprime une référence nutritionnelle et toutes ses relations
     * @param reference La référence à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    suspend fun deleteReference(reference: ReferenceEv): Boolean

    /**
     * Supprime une référence nutritionnelle par son identifiant
     * @param referenceId L'identifiant de la référence à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    suspend fun deleteReferenceById(referenceId: String): Boolean
}

/** Implémentation du repository ReferenceEv */
class DatabaseReferenceEvRepository(
        private val referenceEvDao: ReferenceEvDao,
        private val biblioRefDao: BiblioRefDao
) : ReferenceEvRepository {

    override fun getAllReferences(): Flow<List<ReferenceEv>> {
        return referenceEvDao.getAllReferences().map { references ->
            references.map { referenceEntity ->
                val reference = referenceEntity.toDomain()
                loadRelations(reference)
            }
        }
    }

    override suspend fun getReferenceById(referenceId: String): ReferenceEv? {
        return withContext(AppDispatchers.IO) {
            val referenceEntity =
                    referenceEvDao.getReferenceById(referenceId) ?: return@withContext null
            val reference = referenceEntity.toDomain()
            loadRelations(reference)
        }
    }

    override suspend fun getReferencesByEspeceAndStade(
            espece: String,
            stade: String
    ): List<ReferenceEv> {
        return withContext(AppDispatchers.IO) {
            referenceEvDao.getReferencesByEspeceAndStade(espece, stade).map { referenceEntity ->
                val reference = referenceEntity.toDomain()
                loadRelations(reference)
            }
        }
    }

    override suspend fun insertReference(reference: ReferenceEv): Boolean {
        return withContext(AppDispatchers.IO) {
            try {
                // 1. Insérer la référence
                val referenceId = referenceEvDao.insertReference(reference.toEntity())
                if (referenceId <= 0) return@withContext false

                // 2. Insérer les références bibliographiques
                saveReferenceBiblios(reference)

                // 3. Insérer les nutriments de la référence
                saveReferenceNutriments(reference)

                // 4. Insérer les coefficients de la référence
                saveReferenceCoefficients(reference)

                true
            } catch (e: Exception) {
                println("Erreur lors de l'insertion de la référence: ${e.message}")
                false
            }
        }
    }

    override suspend fun updateReference(reference: ReferenceEv): Boolean {
        return withContext(AppDispatchers.IO) {
            try {
                // 1. Mettre à jour la référence
                referenceEvDao.updateReference(reference.toEntity())

                // 2. Supprimer et réinsérer les relations pour éviter les problèmes de
                // synchronisation
                // Nutriments
                referenceEvDao.deleteAllNutrientReferencesForReference(reference.uuid)
                saveReferenceNutriments(reference)

                // Coefficients
                referenceEvDao.deleteAllCoefficientsForReference(reference.uuid)
                saveReferenceCoefficients(reference)

                // Références bibliographiques
                saveReferenceBiblios(reference)

                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de la référence: ${e.message}")
                false
            }
        }
    }

    override suspend fun deleteReference(reference: ReferenceEv): Boolean {
        return withContext(AppDispatchers.IO) {
            try {
                referenceEvDao.deleteReference(reference.toEntity())
                true
            } catch (e: Exception) {
                println("Erreur lors de la suppression de la référence: ${e.message}")
                false
            }
        }
    }

    override suspend fun deleteReferenceById(referenceId: String): Boolean {
        return withContext(AppDispatchers.IO) {
            try {
                referenceEvDao.deleteReferenceWithRelations(referenceId)
                true
            } catch (e: Exception) {
                println("Erreur lors de la suppression de la référence: ${e.message}")
                false
            }
        }
    }

    /**
     * Charge toutes les relations d'une référence (nutriments, coefficients)
     * @param reference La référence à compléter
     * @return La référence avec ses relations
     */
    private suspend fun loadRelations(reference: ReferenceEv): ReferenceEv {
        // 1. Charger les nutriments
        val nutrientEntities = referenceEvDao.getNutrientReferencesForReference(reference.uuid)

        for (nutrientEntity in nutrientEntities) {
            // Récupérer la référence bibliographique associée si elle existe
            val biblioRef =
                    nutrientEntity.biblioRefId?.let { biblioId ->
                        biblioRefDao.getBiblioRefById(biblioId)?.toDomain()
                    }

            // Ajouter le nutriment à la référence en utilisant le mapper
            val nut4Ref = nutrientEntity.toDomain(biblioRef)
            val nutrient = nut4Ref.nutrient
            val niveauRef = nut4Ref.niveauRef

            // Ajouter le nutriment à la bonne map
            reference.definirNutriment(
                    valeur = nut4Ref.quantite,
                    nutrient = nutrient,
                    niveauRef = niveauRef,
                    uniteReq = nut4Ref.uniteReq,
                    biblio = biblioRef ?: BiblioRef()
            )
        }

        // 2. Charger les coefficients
        for (groupId in 0..4) {
            val coefficients = referenceEvDao.getCoefficientsForGroup(reference.uuid, groupId)
            val coefList = coefficients.map { it.toDomain() }
            reference.updateCoefficientsForGroup(groupId, coefList)
        }

        return reference
    }

    /**
     * Enregistre les références bibliographiques utilisées par une référence
     * @param reference La référence contenant les biblios à sauvegarder
     */
    private suspend fun saveReferenceBiblios(reference: ReferenceEv) {
        // Récupérer toutes les biblios de la référence
        val biblios = reference.obtenirToutesBiblios()

        // Insérer chaque biblio si elle n'existe pas déjà
        for (biblio in biblios) {
            if (biblio.uuid.isNotBlank()) {
                try {
                    // Vérifier si la biblio existe déjà
                    val existingBiblio = biblioRefDao.getBiblioRefById(biblio.uuid)
                    if (existingBiblio == null) {
                        // Insérer la nouvelle biblio
                        biblioRefDao.insertBiblioRef(biblio.toEntity())
                    } else {
                        // Mettre à jour la biblio existante
                        biblioRefDao.updateBiblioRef(biblio.toEntity())
                    }
                } catch (e: Exception) {
                    println(
                            "Erreur lors de la sauvegarde d'une référence bibliographique: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Enregistre les nutriments d'une référence
     * @param reference La référence contenant les nutriments à sauvegarder
     */
    private suspend fun saveReferenceNutriments(reference: ReferenceEv) {
        val nutriments = reference.obtenirTousLesNutriments()
        val nutrimentEntities =
                nutriments.map { nut ->
                    NutrientReferenceEntity(
                            uuid = nut.uuid,
                            referenceId = reference.uuid,
                            nutrient = nut.nutrient.name,
                            niveauRef = nut.niveauRef.name,
                            quantite = nut.quantite,
                            unite = nut.unite.name,
                            uniteReq = nut.uniteReq.name,
                            biblioRefId = nut.citation?.uuid
                    )
                }

        if (nutrimentEntities.isNotEmpty()) {
            referenceEvDao.insertNutrientReferences(nutrimentEntities)
        }
    }

    /**
     * Enregistre les coefficients d'une référence
     * @param reference La référence contenant les coefficients à sauvegarder
     */
    private suspend fun saveReferenceCoefficients(reference: ReferenceEv) {
        val allCoefficients = mutableListOf<CoefficientEntity>()

        // Récupérer tous les coefficients pour chaque groupe
        for (groupId in 0..4) {
            val coefficients = reference.getCoefficientsForGroup(groupId)
            val coefficientEntities =
                    coefficients.map { coef ->
                        CoefficientEntity(
                                uuid = coef.uuid,
                                referenceId = reference.uuid,
                                groupUUID = groupId,
                                description = coef.description,
                                coef = coef.coef
                        )
                    }
            allCoefficients.addAll(coefficientEntities)
        }

        if (allCoefficients.isNotEmpty()) {
            referenceEvDao.insertCoefficients(allCoefficients)
        }
    }
}
