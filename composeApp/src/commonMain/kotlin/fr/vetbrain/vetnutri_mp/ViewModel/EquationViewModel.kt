package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les équations
 *
 * @param equationRepository Repository pour accéder aux équations
 * @param biblioRefDao DAO pour accéder aux références bibliographiques
 */
class EquationViewModel(
        private val equationRepository: EquationRepository,
        private val biblioRefDao: BiblioRefDao,
        private val biblioRepository: BiblioRefRepository
) : ViewModel() {
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    // État d'édition
    private val _currentEquation = MutableStateFlow(Equation())
    val currentEquation: StateFlow<Equation> = _currentEquation.asStateFlow()

    // Liste des références bibliographiques
    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val biblioRefs: StateFlow<List<BiblioRef>> = _biblioRefs.asStateFlow()

    // État de sélection d'une référence bibliographique
    private val _selectedBiblioRef = MutableStateFlow<BiblioRef?>(null)
    val selectedBiblioRef: StateFlow<BiblioRef?> = _selectedBiblioRef.asStateFlow()

    // Messages d'opération
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    // État indiquant si une sauvegarde a été effectuée avec succès
    private val _saveSuccessful = MutableStateFlow(false)
    val saveSuccessful: StateFlow<Boolean> = _saveSuccessful.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Liste des équations disponibles
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())
    val equations: StateFlow<List<Equation>> = _equations.asStateFlow()

    // Script coloré
    private val _coloredScript = MutableStateFlow<String>("")
    val coloredScript: StateFlow<String> = _coloredScript.asStateFlow()

    // Variables d'état privées
    private val _unrecognizedVariables = MutableStateFlow<List<String>>(emptyList())
    val unrecognizedVariables: StateFlow<List<String>> = _unrecognizedVariables.asStateFlow()

    init {
        loadEquations()
        loadBiblioRefs()
    }

    /** Charge toutes les équations disponibles */
    fun loadEquations() {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val result = equationRepository.getAllEquations()
                _equations.value = result
                _isLoading.value = false
            } catch (e: Exception) {
                println("Erreur lors du chargement des équations: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    /** Charge les références bibliographiques */
    fun loadBiblioRefs() {
        coroutineScope.launch {
            try {
                val refs = biblioRefDao.getAllBiblioRefs()
                val mappedRefs = refs.map { ref -> ref.toDomain() }
                _biblioRefs.value = mappedRefs
            } catch (e: Exception) {
                println("Erreur lors du chargement des références biblio: ${e.message}")
            }
        }
    }

    /** Crée une nouvelle équation */
    fun createNewEquation() {
        _currentEquation.value = Equation()
    }

    /** Charge une équation par son ID */
    fun loadEquation(equationId: String) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val equation = equationRepository.getEquationById(equationId)
                if (equation != null) {
                    _currentEquation.value = equation
                } else {
                    _operationMessage.value = "Erreur: Équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Met à jour le nom de l'équation */
    fun updateName(name: String) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(name = name)
    }

    /** Met à jour la description de l'équation */
    fun updateDescription(description: String) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(description = description)
    }

    /** Met à jour le type de l'équation */
    fun updateKind(kind: EquationKind) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(kind = kind)
    }

    /** Met à jour le script de l'équation */
    fun updateEquationScript(script: String) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(equationScript = script)

        // Analyser le script pour détecter automatiquement les variables utilisées
        analyzeScriptForVariables(script)
    }

    /** Analyse le script pour détecter et gérer automatiquement les variables */
    private fun analyzeScriptForVariables(script: String) {
        // Récupérer toutes les variables possibles
        val allVariables = VariableKind.values()

        // Créer une liste de toutes les variables détectées dans le script
        val detectedVariables = mutableListOf<VariableKind>()

        // Liste pour stocker les variables non reconnues
        val unrecognizedVars = mutableListOf<String>()

        // Découper le script en mots
        val words =
                script.split(Regex("\\s+|(?=[+\\-*/^()])|(?<=[+\\-*/^()])")).filter {
                    it.isNotBlank()
                }

        // Pour chaque mot, vérifier s'il ressemble à une variable (commençant par une lettre)
        val potentialVariables = words.filter { it.matches(Regex("^[A-Za-z][A-Za-z0-9]*$")) }

        // Pour chaque variable possible, vérifier si elle est présente dans les mots
        // (correspondance exacte)
        allVariables.forEach { variableKind ->
            if (words.contains(variableKind.variable)) {
                detectedVariables.add(variableKind)
            }
        }

        // Identifier les variables non reconnues
        potentialVariables.forEach { word ->
            if (!allVariables.any { it.variable == word }) {
                unrecognizedVars.add(word)
            }
        }

        println(
                "DEBUG: Variables détectées dans le script: ${detectedVariables.map { it.variable }}"
        )

        println("DEBUG: Variables non reconnues dans le script: $unrecognizedVars")

        // Mettre à jour la liste des variables de l'équation
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(variables = detectedVariables.toMutableList())

        // Mettre à jour la liste des variables non reconnues
        _unrecognizedVariables.value = unrecognizedVars
    }

    /** Met à jour la note bibliographique */
    fun updateBibNote(note: String) {
        val currentValue = _currentEquation.value
        val updatedBib = currentValue.bib.copy(comments = note)
        _currentEquation.value = currentValue.copy(bib = updatedBib)
    }

    /** Met à jour la référence bibliographique */
    fun updateBibRef(ref: String) {
        val currentValue = _currentEquation.value
        val updatedBib = currentValue.bib.copy(completeRef = ref)
        _currentEquation.value = currentValue.copy(bib = updatedBib)
    }

    /** Met à jour le facteur de correction */
    fun updateCorrectionFactor(factor: Double) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(correctionFactor = factor)
    }

    /** Ajoute une variable à l'équation */
    fun addVariable(variableKind: VariableKind) {
        val currentValue = _currentEquation.value
        // Vérifier si la variable existe déjà
        if (!currentValue.variables.contains(variableKind)) {
            val updatedVariables = currentValue.variables.toMutableList()
            updatedVariables.add(variableKind)
            _currentEquation.value = currentValue.copy(variables = updatedVariables)
        }
    }

    /** Supprime une variable de l'équation */
    fun removeVariable(variableKind: VariableKind) {
        val currentValue = _currentEquation.value
        val updatedVariables = currentValue.variables.toMutableList()
        updatedVariables.remove(variableKind)
        _currentEquation.value = currentValue.copy(variables = updatedVariables)
    }

    /** Efface toutes les variables sélectionnées */
    fun clearSelectedVariables() {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(variables = mutableListOf())
    }

    /** Efface le message d'opération */
    fun clearOperationMessage() {
        _operationMessage.value = null
        _saveSuccessful.value = false
    }

    /** Sauvegarde l'équation courante, retourne true si succès, false sinon */
    fun saveCurrentEquation(): Boolean {
        println("DEBUG: Tentative de sauvegarde de l'équation")

        val equation = _currentEquation.value

        // Valider les données
        if (equation.name.isBlank()) {
            _operationMessage.value = "Erreur: nom vide"
            return false
        }

        if (equation.description.isBlank()) {
            _operationMessage.value = "Erreur: description vide"
            return false
        }

        if (equation.equationScript.isBlank()) {
            _operationMessage.value = "Erreur: script vide"
            return false
        }

        // Vérifier si la bibliographie est renseignée
        if (equation.bib.completeRef.isBlank()) {
            _operationMessage.value = "Erreur: référence bibliographique manquante"
            return false
        }

        coroutineScope.launch {
            _isLoading.value = true
            try {
                equationRepository.saveEquation(equation)
                _saveSuccessful.value = true
                _operationMessage.value = "Équation sauvegardée avec succès"
                println("DEBUG: Équation sauvegardée avec succès")
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de la sauvegarde: ${e.message}"
                println("DEBUG: Erreur lors de la sauvegarde - ${e.message}")
                _saveSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }

        return true
    }

    /** Supprime l'équation en cours d'édition */
    fun deleteEquation() {
        if (_currentEquation.value.uuid.isEmpty()) {
            return
        }

        coroutineScope.launch {
            _isLoading.value = true
            try {
                equationRepository.deleteEquation(_currentEquation.value.uuid)
                _operationMessage.value = "Équation supprimée avec succès"
                _saveSuccessful.value = true
                _currentEquation.value = Equation()
                loadEquations()
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de la suppression: ${e.message}"
                _saveSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Réinitialise l'équation en cours d'édition */
    fun clearCurrentEquation() {
        _currentEquation.value = Equation()
        _saveSuccessful.value = false
    }

    /** Sélectionne une référence bibliographique existante */
    fun selectBiblioRef(biblioRef: BiblioRef?) {
        _selectedBiblioRef.value = biblioRef
        _currentEquation.value = _currentEquation.value.copy(bib = biblioRef ?: BiblioRef())
    }
}
