package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Référentiels nutritionnels et mots-clés proposés dans les consultations de la fiche animal.
 * Détenu par [AnimalDetailViewModel], qui en expose l'état et lui délègue les chargements.
 */
internal class ReferencesConsultation(
        private val referenceRepository: DatabaseReferenceEvRepository,
        private val consultationRepository: ConsultationRepository,
        private val scope: CoroutineScope
) {
    private val _availableReferences = MutableStateFlow<List<ReferenceEv>>(emptyList())
    val availableReferences: StateFlow<List<ReferenceEv>> = _availableReferences.asStateFlow()

    private val _isLoadingReferences = MutableStateFlow(false)
    val isLoadingReferences: StateFlow<Boolean> = _isLoadingReferences.asStateFlow()

    private val _availableKeywords = MutableStateFlow<List<ConsultationKeyword>>(emptyList())
    val availableKeywords: StateFlow<List<ConsultationKeyword>> = _availableKeywords.asStateFlow()

    /** Charge tous les référentiels (le filtrage par espèce est fait par les écrans). */
    fun chargerReferencesDisponibles() {
        scope.launch {
            try {
                _isLoadingReferences.value = true
                _availableReferences.value = referenceRepository.getAllReferenceEv()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _availableReferences.value = emptyList()
            } finally {
                _isLoadingReferences.value = false
            }
        }
    }

    fun chargerMotsClesConsultation() {
        scope.launch {
            try {
                _availableKeywords.value = consultationRepository.getAllKeywords()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("ReferencesConsultation", e.message ?: "Erreur", e)
                _availableKeywords.value = emptyList()
            }
        }
    }

    /** Ajoute un mot-clé s'il n'existe pas déjà (comparaison insensible à la casse). */
    fun ajouterMotCleConsultation(keyword: ConsultationKeyword) {
        scope.launch {
            try {
                if (_availableKeywords.value.any { it.label.equals(keyword.label, ignoreCase = true) }) {
                    return@launch
                }
                consultationRepository.saveKeyword(keyword)
                _availableKeywords.value = consultationRepository.getAllKeywords()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("ReferencesConsultation", e.message ?: "Erreur", e)
            }
        }
    }
}
