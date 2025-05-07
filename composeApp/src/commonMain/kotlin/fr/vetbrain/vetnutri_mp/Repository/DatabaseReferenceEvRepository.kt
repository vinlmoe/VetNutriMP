package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.CoefficientDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.updateCoefficientsForGroup
import fr.vetbrain.vetnutri_mp.DataBase.NutrientReferenceDao
import fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository qui implémente la persistance des références nutritionnelles via la base de données
 * Room
 */
class DatabaseReferenceEvRepository(
        private val referenceEvDao: ReferenceEvDao,
        private val coefficientDao: CoefficientDao,
        private val nutrientReferenceDao: NutrientReferenceDao,
        private val biblioRefDao: BiblioRefDao,
        private val equationRepository: EquationRepository
) {
    // Cache des références en mémoire pour éviter des accès fréquents à la base de données
    private val referenceCache = mutableMapOf<String, ReferenceEv>()

    init {
        // Pré-chargement des références au démarrage
        withContext(Dispatchers.IO) {
            try {
                val references = getAllReferences()
                println(
                        "DEBUG DatabaseReferenceEvRepository: ${references.size} références chargées initialement"
                )
            } catch (e: Exception) {
                println(
                        "DEBUG DatabaseReferenceEvRepository: Erreur lors du chargement initial: ${e.message}"
                )
            }
        }
    }

    /** Récupère toutes les références nutritionnelles */
    suspend fun getAllReferences(): List<ReferenceEv> =
            withContext(Dispatchers.IO) {
                val entities = referenceEvDao.getAllReferences()
                return@withContext entities.map { entity ->
                    entity.toDomain(
                            equationRepository = equationRepository,
                            coefficientDao = coefficientDao,
                            nutrientReferenceDao = nutrientReferenceDao
                    )
                }
            }

    /** Récupère une référence nutritionnelle par son identifiant */
    suspend fun getReferenceById(uuid: String): ReferenceEv? =
            withContext(Dispatchers.IO) {
                val entity = referenceEvDao.getReferenceById(uuid) ?: return@withContext null
                return@withContext entity.toDomain(
                        equationRepository = equationRepository,
                        coefficientDao = coefficientDao,
                        nutrientReferenceDao = nutrientReferenceDao
                )
            }

    /** Sauvegarde une référence nutritionnelle */
    suspend fun saveReference(reference: ReferenceEv) =
            withContext(Dispatchers.IO) {
                // Convertir et sauvegarder la référence
                val entity = reference.toEntity()
                referenceEvDao.insertReference(entity)

                // Sauvegarder les coefficients
                saveCoefficients(reference)

                // Sauvegarder les valeurs nutritionnelles
                saveNutrientReferences(reference)
            }

    /** Supprime une référence nutritionnelle */
    suspend fun deleteReference(uuid: String) =
            withContext(Dispatchers.IO) {
                // La suppression de la référence entraînera la suppression en cascade des
                // coefficients et valeurs nutritionnelles
                referenceEvDao.deleteReferenceById(uuid)
            }

    /** Sauvegarde les coefficients d'une référence */
    private suspend fun saveCoefficients(reference: ReferenceEv) {
        // Supprimer tous les coefficients existants
        coefficientDao.deleteCoefficientsForReference(reference.uuid)

        // Sauvegarder les nouveaux coefficients par groupe
        updateCoefficientsForGroup(coefficientDao, reference.uuid, 0, reference.modk1)
        updateCoefficientsForGroup(coefficientDao, reference.uuid, 1, reference.modk2)
        updateCoefficientsForGroup(coefficientDao, reference.uuid, 2, reference.modk3)
        updateCoefficientsForGroup(coefficientDao, reference.uuid, 3, reference.modk4)
        updateCoefficientsForGroup(coefficientDao, reference.uuid, 4, reference.modk5)
    }

    /** Sauvegarde les valeurs nutritionnelles d'une référence */
    private suspend fun saveNutrientReferences(reference: ReferenceEv) {
        // Supprimer toutes les valeurs nutritionnelles existantes
        nutrientReferenceDao.deleteNutrientReferencesForReference(reference.uuid)

        // Convertir et sauvegarder les nouvelles valeurs nutritionnelles
        val entities = reference.nut4RefList.map { it.toEntity(reference.uuid) }
        nutrientReferenceDao.insertNutrientReferences(entities)
    }

    /** Met à jour une référence existante */
    suspend fun updateReference(reference: ReferenceEv): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de la référence ${reference.uuid}"
        )

        // La logique est la même que pour saveReference
        return saveReference(reference)
    }

    /** Met à jour l'équation de poids corporel d'une référence */
    suspend fun updateEquationBW(referenceId: String, equation: Equation): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de l'équation BW pour la référence $referenceId"
        )

        try {
            val reference = getReferenceById(referenceId) ?: return false
            reference.equationBW = equation
            return updateReference(reference)
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour de l'équation BW: ${e.message}"
            )
            return false
        }
    }

    /** Met à jour l'équation de besoin énergétique de base d'une référence */
    suspend fun updateEquationBEE(referenceId: String, equation: Equation): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de l'équation BEE pour la référence $referenceId"
        )

        try {
            val reference = getReferenceById(referenceId) ?: return false
            reference.equationBEE = equation
            return updateReference(reference)
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour de l'équation BEE: ${e.message}"
            )
            return false
        }
    }

    /** Met à jour l'équation d'énergie digestible commerciale d'une référence */
    suspend fun updateEquationDEcom(referenceId: String, equation: Equation): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de l'équation DEcom pour la référence $referenceId"
        )

        try {
            val reference = getReferenceById(referenceId) ?: return false
            reference.equationDEcom = equation
            return updateReference(reference)
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour de l'équation DEcom: ${e.message}"
            )
            return false
        }
    }

    /** Met à jour l'équation d'énergie digestible brute d'une référence */
    suspend fun updateEquationDEraw(referenceId: String, equation: Equation): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de l'équation DEraw pour la référence $referenceId"
        )

        try {
            val reference = getReferenceById(referenceId) ?: return false
            reference.equationDEraw = equation
            return updateReference(reference)
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour de l'équation DEraw: ${e.message}"
            )
            return false
        }
    }

    /** Met à jour l'équation d'énergie métabolisable d'une référence */
    suspend fun updateEquationME(referenceId: String, equation: Equation): Boolean {
        println(
                "DEBUG DatabaseReferenceEvRepository: Mise à jour de l'équation ME pour la référence $referenceId"
        )

        try {
            val reference = getReferenceById(referenceId) ?: return false
            reference.equationME = equation
            return updateReference(reference)
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour de l'équation ME: ${e.message}"
            )
            return false
        }
    }
}
