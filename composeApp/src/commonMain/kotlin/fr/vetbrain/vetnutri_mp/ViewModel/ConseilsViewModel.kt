package fr.vetbrain.vetnutri_mp.ViewModel

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<SectionCategory?>(null)
        private set

    init {
        loadConseils()
    }

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
                _error.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchConseils(query: String) {
        searchQuery = query
    }

    fun filterByCategory(category: SectionCategory?) {
        selectedCategory = category
    }

    fun getFilteredConseils(): List<HtmlSection> {
        return _conseils.value.filter { conseil ->
            val matchesSearch =
                    searchQuery.isEmpty() ||
                            conseil.title.contains(searchQuery, ignoreCase = true) ||
                            conseil.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCategory = selectedCategory == null || conseil.category == selectedCategory

            matchesSearch && matchesCategory
        }
    }

    fun createConseil(title: String, category: SectionCategory) {
        coroutineScope.launch {
            try {
                val nouveauConseil =
                        HtmlSection(
                                id = "conseil_${Clock.System.now().toEpochMilliseconds()}",
                                title = title,
                                content =
                                        fr.vetbrain.vetnutri_mp.Export.RichTextContent(
                                                blocks =
                                                        listOf(
                                                                fr.vetbrain.vetnutri_mp.Export
                                                                        .TextBlock.Paragraph(
                                                                        id = "p1",
                                                                        text =
                                                                                "Contenu du conseil...",
                                                                        formatting =
                                                                                fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Export
                                                                                        .TextFormatting()
                                                                )
                                                        )
                                        ),
                                category = category,
                                tags = emptyList(),
                                priority = 0,
                                isActive = true,
                                targetSpecies = emptyList(),
                                targetAgeGroups = emptyList()
                        )

                val result = conseilRepository.sauvegarderConseil(nouveauConseil)
                if (result.isSuccess) {
                    loadConseils() // Recharger la liste
                } else {
                    _error.value = "Erreur lors de la création du conseil"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
        }
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
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    fun incrementUsage(conseilId: String) {
        coroutineScope.launch {
            try {
                conseilRepository.incrementerUsage(conseilId)
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
