package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Export.*
import fr.vetbrain.vetnutri_mp.Repository.HtmlSectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel pour gérer les sections HTML réutilisables */
class HtmlSectionViewModel(private val repository: HtmlSectionRepository) : ViewModel() {

    // États pour l'interface utilisateur
    private val _sections = MutableStateFlow<List<HtmlSection>>(emptyList())
    val sections: StateFlow<List<HtmlSection>> = _sections.asStateFlow()

    private val _selectedSection = MutableStateFlow<HtmlSection?>(null)
    val selectedSection: StateFlow<HtmlSection?> = _selectedSection.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<SectionCategory?>(null)
    val selectedCategory: StateFlow<SectionCategory?> = _selectedCategory.asStateFlow()

    private val _showTemplatesOnly = MutableStateFlow(false)
    val showTemplatesOnly: StateFlow<Boolean> = _showTemplatesOnly.asStateFlow()

    init {
        loadSections()
    }

    /** Charge toutes les sections */
    fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getAllSections()
                result.onSuccess { sections -> _sections.value = sections }.onFailure { error ->
                    // Gérer l'erreur (logging, message utilisateur, etc.)
                    println("Erreur lors du chargement des sections: ${error.message}")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Sélectionne une section */
    fun selectSection(section: HtmlSection?) {
        _selectedSection.value = section
    }

    /** Crée une nouvelle section */
    fun createSection(
            title: String,
            category: SectionCategory = SectionCategory.GENERAL,
            content: RichTextContent = RichTextContent()
    ) {
        viewModelScope.launch {
            val newSection =
                    HtmlSection(id = "", title = title, content = content, category = category)
            val result = repository.saveSection(newSection)
            result.onSuccess { loadSections() }.onFailure { error ->
                println("Erreur lors de la création de la section: ${error.message}")
            }
        }
    }

    /** Met à jour une section existante */
    fun updateSection(section: HtmlSection) {
        viewModelScope.launch {
            val result = repository.updateSection(section)
            result
                    .onSuccess {
                        loadSections()
                        if (_selectedSection.value?.id == section.id) {
                            _selectedSection.value = section
                        }
                    }
                    .onFailure { error ->
                        println("Erreur lors de la mise à jour de la section: ${error.message}")
                    }
        }
    }

    /** Supprime une section */
    fun deleteSection(sectionId: String) {
        viewModelScope.launch {
            val result = repository.deleteSection(sectionId)
            result
                    .onSuccess {
                        loadSections()
                        if (_selectedSection.value?.id == sectionId) {
                            _selectedSection.value = null
                        }
                    }
                    .onFailure { error ->
                        println("Erreur lors de la suppression de la section: ${error.message}")
                    }
        }
    }

    /** Duplique une section */
    fun duplicateSection(sectionId: String, newTitle: String) {
        viewModelScope.launch {
            val result = repository.duplicateSection(sectionId, newTitle)
            result
                    .onSuccess { duplicatedSection ->
                        loadSections()
                        _selectedSection.value = duplicatedSection
                    }
                    .onFailure { error ->
                        println("Erreur lors de la duplication de la section: ${error.message}")
                    }
        }
    }

    /** Crée une section à partir d'un modèle */
    fun createFromTemplate(templateId: String, newTitle: String) {
        viewModelScope.launch {
            val result = repository.createFromTemplate(templateId, newTitle)
            result
                    .onSuccess { newSection ->
                        loadSections()
                        _selectedSection.value = newSection
                    }
                    .onFailure { error ->
                        println("Erreur lors de la création depuis le modèle: ${error.message}")
                    }
        }
    }

    /** Recherche des sections */
    fun searchSections(query: String) {
        _searchQuery.value = query
        // La logique de filtrage sera gérée dans l'UI en combinant avec les autres filtres
    }

    /** Filtre par catégorie */
    fun filterByCategory(category: SectionCategory?) {
        _selectedCategory.value = category
    }

    /** Bascule l'affichage des modèles uniquement */
    fun toggleTemplatesOnly() {
        _showTemplatesOnly.value = !_showTemplatesOnly.value
    }

    /** Obtient les sections filtrées selon les critères actuels */
    fun getFilteredSections(): List<HtmlSection> {
        return _sections.value.filter { section ->
            // Filtre par recherche
            val matchesSearch =
                    _searchQuery.value.isBlank() ||
                            section.title.contains(_searchQuery.value, ignoreCase = true) ||
                            section.tags.any { it.contains(_searchQuery.value, ignoreCase = true) }

            // Filtre par catégorie
            val matchesCategory =
                    _selectedCategory.value == null || section.category == _selectedCategory.value

            // Filtre par modèles uniquement
            val matchesTemplateFilter = !_showTemplatesOnly.value || section.isTemplate

            matchesSearch && matchesCategory && matchesTemplateFilter
        }
    }

    /** Obtient les sections par catégorie pour l'affichage groupé */
    fun getSectionsByCategory(): Map<SectionCategory, List<HtmlSection>> {
        return getFilteredSections().groupBy { it.category }
    }

    /** Exporte une section au format HTML */
    fun exportSectionToHtml(section: HtmlSection): String {
        return HtmlSectionParser.parseSectionToHtml(section)
    }

    /** Prévisualise une section (retourne le HTML complet avec CSS) */
    fun previewSection(section: HtmlSection): String {
        val sectionHtml = exportSectionToHtml(section)
        val css = HtmlSectionParser.generateSectionCss()
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'/>
                <style>$css</style>
                <title>Prévisualisation - ${section.title}</title>
            </head>
            <body>
                $sectionHtml
            </body>
            </html>
        """.trimIndent()
    }
}



