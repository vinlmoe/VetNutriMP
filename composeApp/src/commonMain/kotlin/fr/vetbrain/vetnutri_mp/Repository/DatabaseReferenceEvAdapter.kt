package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.Nut4Ref
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlinx.coroutines.runBlocking

/**
 * Adaptateur qui implémente l'interface ReferenceEvRepository en utilisant le
 * DatabaseReferenceEvRepository pour la persistance en base de données
 */
class DatabaseReferenceEvAdapter(private val databaseRepository: DatabaseReferenceEvRepository) :
        ReferenceEvRepository() {

    // Surcharge des méthodes de l'interface ReferenceEvRepository pour déléguer au
    // DatabaseReferenceEvRepository

    override fun addref(reference: ReferenceEv): Boolean {
        return runBlocking {
            try {
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de l'ajout de la référence: ${e.message}")
                false
            }
        }
    }

    override fun getAll(): List<ReferenceEv> {
        return runBlocking {
            try {
                databaseRepository.getAllReferences()
            } catch (e: Exception) {
                println("Erreur lors de la récupération des références: ${e.message}")
                emptyList()
            }
        }
    }

    override fun getByUuid(uuid: String): ReferenceEv? {
        return runBlocking {
            try {
                databaseRepository.getReferenceById(uuid)
            } catch (e: Exception) {
                println("Erreur lors de la récupération de la référence $uuid: ${e.message}")
                null
            }
        }
    }

    override fun updateRef(reference: ReferenceEv): Boolean {
        return runBlocking {
            try {
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de la référence: ${e.message}")
                false
            }
        }
    }

    override fun updateEquationBW(refUuid: String, equation: Equation?): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                reference.equationBW = equation
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'équation BW: ${e.message}")
                false
            }
        }
    }

    override fun updateEquationBEE(refUuid: String, equation: Equation?): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                reference.equationBEE = equation
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'équation BEE: ${e.message}")
                false
            }
        }
    }

    override fun updateEquationDEcom(refUuid: String, equation: Equation?): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                reference.equationDEcom = equation
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'équation DEcom: ${e.message}")
                false
            }
        }
    }

    override fun updateEquationDEraw(refUuid: String, equation: Equation?): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                reference.equationDEraw = equation
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'équation DEraw: ${e.message}")
                false
            }
        }
    }

    override fun updateEquationME(refUuid: String, equation: Equation?): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                reference.equationME = equation
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'équation ME: ${e.message}")
                false
            }
        }
    }

    override fun delRef(uuid: String): Boolean {
        return runBlocking {
            try {
                databaseRepository.deleteReference(uuid)
                true
            } catch (e: Exception) {
                println("Erreur lors de la suppression de la référence: ${e.message}")
                false
            }
        }
    }

    override fun addAllRef(references: List<ReferenceEv>): Boolean {
        var success = true
        references.forEach { reference ->
            val result = addref(reference)
            success = success && result
        }
        return success
    }

    override fun definirNutriment(
            refUuid: String,
            nutrient: Nutrient,
            niveau: Reflevel,
            quantite: Float,
            unite: UnitEnum,
            uniteReq: UnitReqEnum,
            biblioUuid: String?
    ): Boolean {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid) ?: return@runBlocking false
                val nutRef =
                        reference.definirNutriment(
                                nutrient,
                                niveau,
                                quantite,
                                unite,
                                uniteReq,
                                biblioUuid
                        )
                databaseRepository.saveReference(reference)
                true
            } catch (e: Exception) {
                println("Erreur lors de la définition du nutriment: ${e.message}")
                false
            }
        }
    }

    override fun getNutrientsForGroup(refUuid: String, level: Reflevel): List<Nut4Ref> {
        return runBlocking {
            try {
                val reference =
                        databaseRepository.getReferenceById(refUuid)
                                ?: return@runBlocking emptyList()
                reference.getNutrientsForGroup(level)
            } catch (e: Exception) {
                println("Erreur lors de la récupération des nutriments: ${e.message}")
                emptyList()
            }
        }
    }
}
