package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/** Repository pour la gestion des références évaluées (ReferenceEv). */
class ReferenceEvRepository {
    // Stockage temporaire en mémoire
    private val referenceEvMap = mutableMapOf<String, ReferenceEv>()

    /** Obtenir toutes les références évaluées. */
    fun getAll(): List<ReferenceEv> {
        return referenceEvMap.values.toList()
    }

    /** Obtenir une référence évaluée par son identifiant. */
    fun getById(id: String): ReferenceEv? {
        return referenceEvMap[id]
    }

    /** Créer une nouvelle référence évaluée. */
    fun create(referenceEv: ReferenceEv): Boolean {
        try {
            val newUuid = generateUuid()
            val newReferenceEv = ReferenceEv(newUuid)
            // Copier les propriétés de base
            newReferenceEv.nom = referenceEv.nom
            newReferenceEv.description = referenceEv.description
            newReferenceEv.espece = referenceEv.espece
            newReferenceEv.stadePhysio = referenceEv.stadePhysio
            newReferenceEv.nomEnergie = referenceEv.nomEnergie
            referenceEvMap[newUuid] = newReferenceEv
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** Mettre à jour une référence évaluée existante. */
    fun update(referenceEv: ReferenceEv): Boolean {
        try {
            if (referenceEv.uuid.isBlank() || !referenceEvMap.containsKey(referenceEv.uuid)) {
                return false
            }

            referenceEvMap[referenceEv.uuid] = referenceEv
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** Supprimer une référence évaluée par son identifiant. */
    fun delete(id: String): Boolean {
        try {
            if (id.isBlank() || !referenceEvMap.containsKey(id)) {
                return false
            }

            referenceEvMap.remove(id)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** Met à jour l'équation de poids corporel d'une référence. */
    fun updateEquationBW(referenceId: String, equation: Equation): Boolean {
        try {
            val reference = getById(referenceId) ?: return false
            reference.equationBW = equation
            return update(reference)
        } catch (e: Exception) {
            return false
        }
    }

    /** Met à jour l'équation de besoin énergétique de base d'une référence. */
    fun updateEquationBEE(referenceId: String, equation: Equation): Boolean {
        try {
            val reference = getById(referenceId) ?: return false
            reference.equationBEE = equation
            return update(reference)
        } catch (e: Exception) {
            return false
        }
    }

    /** Met à jour l'équation d'énergie digestible commerciale d'une référence. */
    fun updateEquationDEcom(referenceId: String, equation: Equation): Boolean {
        try {
            val reference = getById(referenceId) ?: return false
            reference.equationDEcom = equation
            return update(reference)
        } catch (e: Exception) {
            return false
        }
    }

    /** Met à jour l'équation d'énergie digestible brute d'une référence. */
    fun updateEquationDEraw(referenceId: String, equation: Equation): Boolean {
        try {
            val reference = getById(referenceId) ?: return false
            reference.equationDEraw = equation
            return update(reference)
        } catch (e: Exception) {
            return false
        }
    }

    /** Met à jour l'équation d'énergie métabolisable d'une référence. */
    fun updateEquationME(referenceId: String, equation: Equation): Boolean {
        try {
            val reference = getById(referenceId) ?: return false
            reference.equationME = equation
            return update(reference)
        } catch (e: Exception) {
            return false
        }
    }

    /** Génère un identifiant unique. */
    private fun generateUuid(): String {
        return genUUID()
    }
}
