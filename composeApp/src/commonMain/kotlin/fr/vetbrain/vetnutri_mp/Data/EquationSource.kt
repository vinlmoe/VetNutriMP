package fr.vetbrain.vetnutri_mp.Data

/**
 * Lecture des équations dont ont besoin les calculs (énergie, nutriments, exports) : le domaine
 * ne dépend pas du dépôt complet (écriture, observation), seulement de ces deux lectures.
 * [fr.vetbrain.vetnutri_mp.Repository.EquationRepository] l'implémente.
 */
interface EquationSource {
    /** Toutes les équations */
    suspend fun getAllEquations(): List<Equation>

    /** Équation d'identifiant [uuid], null si absente */
    suspend fun getEquationById(uuid: String): Equation?
}
