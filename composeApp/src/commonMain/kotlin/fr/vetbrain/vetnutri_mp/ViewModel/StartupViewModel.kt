package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.Stable
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository

/**
 * ViewModel de l'écran de démarrage (View/StartupScreen.kt).
 *
 * Expose les compteurs de la base (aliments, référentiels, conseils, animaux), le statut
 * agrégé et l'assainissement des coefficients K dupliqués des référentiels ; l'écran n'accède
 * plus aux repositories (ni directement, ni via SettingsViewModel).
 *
 * L'ancienne version (états `StartupUiState` jamais consommés par l'écran, dépendance à
 * SettingsViewModel) était obsolète et n'était instanciée nulle part : remplacée.
 */
class StartupViewModel(
        private val foodRepository: FoodRepository,
        private val animalRepository: AnimalRepository,
        private val referenceRepository: DatabaseReferenceEvRepository?,
        private val conseilRepository: ConseilRepository?
) : BaseViewModel() {

    suspend fun getFoodsCount(): Int = foodRepository.getFoodsCount()

    suspend fun getReferenceCount(): Int = referenceRepository?.getReferenceCount() ?: 0

    /** Nombre de conseils personnalisés, 0 si indisponible ou en erreur. */
    suspend fun getConseilsCount(): Int =
            try {
                conseilRepository?.getConseilsCount()?.getOrThrow() ?: 0
            } catch (_: Exception) {
                0
            }

    suspend fun getAnimalsCount(): Int = animalRepository.getAnimalsCount()

    /** Statut agrégé de la base de données (aliments, référentiels, conseils). */
    suspend fun loadDatabaseStatus(): DatabaseStatus {
        val foodCount = getFoodsCount()
        val referenceCount = getReferenceCount()
        return DatabaseStatus(
                foodCount = foodCount,
                referenceCount = referenceCount,
                conseilsCount = getConseilsCount(),
                needsUpdate = foodCount == 0 || referenceCount == 0
        )
    }

    /**
     * Assainit les doublons de coefficients K (même nom et même valeur) de tous les référentiels
     * et ne persiste que ceux qui ont été modifiés.
     * @return nombre de référentiels mis à jour
     */
    suspend fun dedupliquerCoefficientsReferentiels(): Int {
        val repository = referenceRepository ?: return 0
        var modifies = 0
        for (ref in repository.getAllReferenceEv()) {
            val before = listOf(ref.modk1.size, ref.modk2.size, ref.modk3.size, ref.modk4.size, ref.modk5.size)
            ref.deduplicateCoefficients()
            val after = listOf(ref.modk1.size, ref.modk2.size, ref.modk3.size, ref.modk4.size, ref.modk5.size)
            if (after != before) {
                repository.updateReferenceEv(ref)
                modifies++
            }
        }
        return modifies
    }
}

/** État de la base de données affiché par l'écran de démarrage. */
@Stable
data class DatabaseStatus(
        val foodCount: Int,
        val referenceCount: Int,
        val conseilsCount: Int = 0,
        val needsUpdate: Boolean,
        val error: String? = null
)
