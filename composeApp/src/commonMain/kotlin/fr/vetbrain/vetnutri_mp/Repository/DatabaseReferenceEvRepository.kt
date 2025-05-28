package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Repository pour la persistance des références évaluées avec Room Multiplatform Utilise la
 * composition pour la compatibilité avec les ViewModels existants
 */
class DatabaseReferenceEvRepository(
        private val referenceEvDao: ReferenceEvDao,
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) : ReferenceEvRepositoryInterface {

    // Repository en mémoire pour la compatibilité
    private val memoryRepository = ReferenceEvRepository()

    init {
        // Ajouter des données de test si la base est vide
        runBlocking {
            try {
                val existingRefs = getAllReferenceEv()
                if (existingRefs.isEmpty()) {
                    println(
                            "DEBUG DatabaseReferenceEvRepository: Base vide, ajout de données de test"
                    )

                    val testRef1 =
                            ReferenceEv(
                                            uuid = "ref-test-1",
                                            nom = "Référence Chien Adulte",
                                            description =
                                                    "Référence nutritionnelle pour chien adulte en bonne santé",
                                            maladie = false,
                                            nomMaladie = "",
                                            nomEnergie = "Énergie d'entretien",
                                            consistent = 1,
                                            espece = Espece.CHIEN,
                                            stadePhysio = StadePhysio.ADULTE
                                    )
                                    .apply {
                                        nomk1 = "Facteur activité"
                                        nomk2 = "Facteur environnement"
                                        nomk3 = "Facteur race"
                                        nomk4 = "Facteur âge"
                                        nomk5 = "Facteur santé"
                                    }

                    val testRef2 =
                            ReferenceEv(
                                            uuid = "ref-test-2",
                                            nom = "Référence Chat Adulte",
                                            description =
                                                    "Référence nutritionnelle pour chat adulte en bonne santé",
                                            maladie = false,
                                            nomMaladie = "",
                                            nomEnergie = "Énergie d'entretien féline",
                                            consistent = 1,
                                            espece = Espece.CHAT,
                                            stadePhysio = StadePhysio.ADULTE
                                    )
                                    .apply {
                                        nomk1 = "Facteur activité"
                                        nomk2 = "Facteur environnement"
                                        nomk3 = "Facteur race"
                                        nomk4 = "Facteur âge"
                                        nomk5 = "Facteur santé"
                                    }

                    saveReferenceEv(testRef1)
                    saveReferenceEv(testRef2)

                    println("DEBUG DatabaseReferenceEvRepository: 2 références de test ajoutées")
                }
            } catch (e: Exception) {
                println(
                        "DEBUG DatabaseReferenceEvRepository: Erreur lors de l'initialisation: ${e.message}"
                )
            }
        }
    }

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

    // Méthodes de compatibilité avec ReferenceEvRepository
    suspend fun getAll(): List<ReferenceEv> {
        return getAllReferenceEv()
    }

    suspend fun getById(id: String): ReferenceEv? {
        return getReferenceEvById(id)
    }

    suspend fun create(reference: ReferenceEv): Boolean {
        return try {
            saveReferenceEv(reference)
            true
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur lors de la création: ${e.message}")
            false
        }
    }

    suspend fun update(reference: ReferenceEv): Boolean {
        return try {
            updateReferenceEv(reference)
            true
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la mise à jour: ${e.message}"
            )
            false
        }
    }

    suspend fun delete(id: String): Boolean {
        return try {
            deleteReferenceEv(id)
            true
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseReferenceEvRepository: Erreur lors de la suppression: ${e.message}"
            )
            false
        }
    }

    // Méthodes de délégation pour les équations (compatibilité avec ReferenceEvRepository)
    fun updateEquationBW(referenceId: String, equation: Equation): Boolean {
        return try {
            runBlocking {
                val reference = getReferenceEvById(referenceId)
                if (reference != null) {
                    reference.equationBW = equation
                    updateReferenceEv(reference)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur updateEquationBW: ${e.message}")
            false
        }
    }

    fun updateEquationBEE(referenceId: String, equation: Equation): Boolean {
        return try {
            runBlocking {
                val reference = getReferenceEvById(referenceId)
                if (reference != null) {
                    reference.equationBEE = equation
                    updateReferenceEv(reference)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur updateEquationBEE: ${e.message}")
            false
        }
    }

    fun updateEquationDEcom(referenceId: String, equation: Equation): Boolean {
        return try {
            runBlocking {
                val reference = getReferenceEvById(referenceId)
                if (reference != null) {
                    reference.equationDEcom = equation
                    updateReferenceEv(reference)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur updateEquationDEcom: ${e.message}")
            false
        }
    }

    fun updateEquationDEraw(referenceId: String, equation: Equation): Boolean {
        return try {
            runBlocking {
                val reference = getReferenceEvById(referenceId)
                if (reference != null) {
                    reference.equationDEraw = equation
                    updateReferenceEv(reference)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur updateEquationDEraw: ${e.message}")
            false
        }
    }

    fun updateEquationME(referenceId: String, equation: Equation): Boolean {
        return try {
            runBlocking {
                val reference = getReferenceEvById(referenceId)
                if (reference != null) {
                    reference.equationME = equation
                    updateReferenceEv(reference)
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseReferenceEvRepository: Erreur updateEquationME: ${e.message}")
            false
        }
    }

    // Méthodes privées pour la conversion

    private suspend fun convertEntityToReferenceEv(entity: ReferenceEvEntity): ReferenceEv {
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
