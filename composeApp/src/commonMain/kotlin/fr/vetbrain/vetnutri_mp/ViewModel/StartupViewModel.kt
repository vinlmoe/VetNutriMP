package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation

/**
 * Accès aux données de l'écran de démarrage : état de la base (comptages) et assainissement
 * des référentiels. `StartupScreen` passe par ici au lieu d'appeler les Repository.
 */
class StartupViewModel(
    private val foodRepository: FoodRepository,
    private val animalRepository: AnimalRepository,
    private val referenceRepository: DatabaseReferenceEvRepository?,
    private val conseilRepository: ConseilRepository?
) {
    /** Nombre d'aliments en base (requête COUNT, sans charger les aliments). */
    suspend fun countFoods(): Int = foodRepository.getFoodsCount()

    suspend fun countReferences(): Int = referenceRepository?.getAllReferenceEv()?.size ?: 0

    /** Nombre de conseils ; 0 si la table est inaccessible (non bloquant au démarrage). */
    suspend fun countConseils(): Int =
        try {
            conseilRepository?.getConseilsCount()?.getOrThrow() ?: 0
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.w(TAG, "Comptage des conseils impossible", e)
            0
        }

    suspend fun isAnimalBaseEmpty(): Boolean = animalRepository.getAnimalsCount() == 0

    /**
     * Supprime les doublons de coefficients K (même nom et même valeur) dans les référentiels ;
     * seuls les référentiels modifiés sont réécrits.
     */
    suspend fun deduplicateReferenceCoefficients() {
        val repository = referenceRepository ?: return
        for (ref in repository.getAllReferenceEv()) {
            val before = listOf(ref.modk1.size, ref.modk2.size, ref.modk3.size, ref.modk4.size, ref.modk5.size)
            ref.deduplicateCoefficients()
            val after = listOf(ref.modk1.size, ref.modk2.size, ref.modk3.size, ref.modk4.size, ref.modk5.size)
            if (after != before) repository.updateReferenceEv(ref)
        }
    }

    private companion object {
        const val TAG = "StartupViewModel"
    }
}
