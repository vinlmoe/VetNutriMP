package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Export.SectionCategory
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ConseilsViewModel(
        private val conseilRepository: ConseilRepository,
        private val coroutineScope: CoroutineScope
) {
    private val _conseils = MutableStateFlow<List<HtmlSection>>(emptyList())
    val conseils: StateFlow<List<HtmlSection>> = _conseils.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<SectionCategory?>(null)
        private set

    fun loadConseils() {
        coroutineScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = conseilRepository.getConseilsActifs()
                if (result.isSuccess) {
                    _conseils.value = result.getOrThrow()
                } else {
                    _error.value = "Erreur lors du chargement des conseils"
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _error.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchConseils(query: String) {
        searchQuery = query
    }

    fun deleteConseil(conseilId: String) {
        coroutineScope.launch {
            try {
                val result = conseilRepository.supprimerConseil(conseilId)
                if (result.isSuccess) {
                    loadConseils() // Recharger la liste
                } else {
                    _error.value = "Erreur lors de la suppression du conseil"
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    /** Conseils actifs (édition d'un conseil existant) */
    suspend fun getActiveAdvices(): Result<List<HtmlSection>> = conseilRepository.getConseilsActifs()

    /** Crée ou met à jour un conseil */
    suspend fun saveAdvice(conseil: HtmlSection) = conseilRepository.sauvegarderConseil(conseil)

    fun clearError() {
        _error.value = null
    }

    private val _templates = MutableStateFlow<List<HtmlSection>>(emptyList())
    val templates: StateFlow<List<HtmlSection>> = _templates.asStateFlow()

    private val _templatesLoading = MutableStateFlow(false)
    val templatesLoading: StateFlow<Boolean> = _templatesLoading.asStateFlow()

    fun loadTemplates() {
        coroutineScope.launch {
            _templatesLoading.value = true
            try {
                conseilRepository.getTemplatesConseils()
                        .onSuccess { _templates.value = it }
                        .onFailure { _error.value = "Erreur: ${it.message}" }
            } finally {
                _templatesLoading.value = false
            }
        }
    }

    /** Crée un conseil à partir d'un modèle puis le transmet à [onCreated] (ouverture en édition). */
    fun createFromTemplate(template: HtmlSection, onCreated: (HtmlSection) -> Unit) {
        coroutineScope.launch {
            conseilRepository
                    .creerConseilDepuisTemplate(templateId = template.id, titre = template.title)
                    .onSuccess { nouveau ->
                        _conseils.value = _conseils.value + nouveau
                        onCreated(nouveau)
                    }
                    .onFailure { _error.value = "Erreur: ${it.message}" }
        }
    }
}
