package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

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

    /** Génère un identifiant unique. */
    private fun generateUuid(): String {
        return (System.currentTimeMillis() + (Math.random() * 10000).toInt()).toString()
    }
}
