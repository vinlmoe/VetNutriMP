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
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
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
 * @param biblioRepository Repository pour accéder aux références bibliographiques
 * @param referenceRepository Repository pour accéder aux références
 */
class EquationViewModel(
        private val equationRepository: EquationRepository,
        private val biblioRefDao: BiblioRefDao?,
        private val biblioRepository: BiblioRefRepository,
        private val referenceRepository: DatabaseReferenceEvRepository
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

    // Équations spécifiques actuellement sélectionnées (pour les combobox)
    private val _equationBW = MutableStateFlow<Equation?>(null)
    val equationBW: StateFlow<Equation?> = _equationBW.asStateFlow()

    private val _equationBEE = MutableStateFlow<Equation?>(null)
    val equationBEE: StateFlow<Equation?> = _equationBEE.asStateFlow()

    private val _equationME = MutableStateFlow<Equation?>(null)
    val equationME: StateFlow<Equation?> = _equationME.asStateFlow()

    private val _equationDEcom = MutableStateFlow<Equation?>(null)
    val equationDEcom: StateFlow<Equation?> = _equationDEcom.asStateFlow()

    private val _equationDEraw = MutableStateFlow<Equation?>(null)
    val equationDEraw: StateFlow<Equation?> = _equationDEraw.asStateFlow()

    // Script coloré
    private val _coloredScript = MutableStateFlow<String>("")
    val coloredScript: StateFlow<String> = _coloredScript.asStateFlow()

    // Variables d'état privées
    private val _unrecognizedVariables = MutableStateFlow<List<String>>(emptyList())
    val unrecognizedVariables: StateFlow<List<String>> = _unrecognizedVariables.asStateFlow()

    // État pour la référence courante
    private val _currentReferenceId = MutableStateFlow<String?>(null)
    val currentReferenceId: StateFlow<String?> = _currentReferenceId.asStateFlow()

    init {
        loadEquations()
        loadBiblioRefs()
    }

    /** Charge la liste des équations disponibles */
    fun loadEquations() {
        coroutineScope.launch(AppDispatchers.IO) {
            try {
                _isLoading.value = true
                _operationMessage.value = null

                // Charger toutes les équations du repository
                val equationsList = equationRepository.getAllEquations()
                _equations.value = equationsList

                // Si une référence est sélectionnée, charger ses équations spécifiques
                val referenceId = _currentReferenceId.value
                if (referenceId != null && referenceId.isNotEmpty()) {
                    // Charger la référence pour obtenir ses équations actuelles
                    referenceRepository.getById(referenceId)?.let { reference ->
                        // Trouver les équations correspondantes dans la liste chargée
                        _equationBW.value =
                                equationsList.find { equation ->
                                    equation.uuid == reference.equationBW?.uuid
                                }
                        _equationBEE.value =
                                equationsList.find { equation ->
                                    equation.uuid == reference.equationBEE?.uuid
                                }
                        _equationME.value =
                                equationsList.find { equation ->
                                    equation.uuid == reference.equationME?.uuid
                                }
                        _equationDEcom.value =
                                equationsList.find { equation ->
                                    equation.uuid == reference.equationDEcom?.uuid
                                }
                        _equationDEraw.value =
                                equationsList.find { equation ->
                                    equation.uuid == reference.equationDEraw?.uuid
                                }
                    }
                }

                println("Nombre d'équations chargées: ${equationsList.size}")
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement des équations: ${e.message}"
                println("Erreur lors du chargement des équations: ${e.message}")
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Charge les références bibliographiques */
    fun loadBiblioRefs() {
        coroutineScope.launch {
            try {
                if (biblioRefDao != null) {
                    val refs = biblioRefDao.getAllBiblioRefs()
                    val mappedRefs = refs.map { ref -> ref.toDomain() }
                    _biblioRefs.value = mappedRefs
                } else {
                    // Utiliser le repository si le DAO n'est pas disponible
                    biblioRepository.getAllBiblioRefs().collect { refs -> _biblioRefs.value = refs }
                }
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

    /** Définit la référence actuellement sélectionnée */
    fun setCurrentReferenceId(id: String?) {
        _currentReferenceId.value = id
    }

    /** Met à jour l'équation de poids corporel pour la référence courante. */
    fun setEquationBW(referenceId: String, equationId: String) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val reference = referenceRepository.getById(referenceId)
                val equation = equationRepository.getEquationById(equationId)

                if (reference != null && equation != null) {
                    reference.equationBW = equation
                    referenceRepository.update(reference)
                    _currentReferenceId.value = referenceId
                    _operationMessage.value = "Équation de poids corporel mise à jour avec succès"
                } else {
                    _operationMessage.value = "Référence ou équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour l'équation de poids corporel pour la référence courante avec l'équation fournie.
     */
    fun setEquationBW(equation: Equation?) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val referenceId = _currentReferenceId.value
                if (referenceId.isNullOrEmpty()) {
                    _operationMessage.value = "Aucune référence sélectionnée"
                    return@launch
                }

                if (equation == null) {
                    val reference = referenceRepository.getById(referenceId)
                    if (reference != null) {
                        reference.equationBW = Equation()
                        referenceRepository.update(reference)
                        _equationBW.value = null
                        _operationMessage.value = "Équation de poids corporel supprimée"
                    } else {
                        _operationMessage.value = "Référence non trouvée"
                    }
                } else {
                    val success = referenceRepository.updateEquationBW(referenceId, equation)
                    if (success) {
                        _equationBW.value = equation
                        _operationMessage.value =
                                "Équation de poids corporel mise à jour avec succès"
                    } else {
                        _operationMessage.value = "Échec de la mise à jour de l'équation"
                    }
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Met à jour l'équation de besoin énergétique de base pour la référence courante. */
    fun setEquationBEE(referenceId: String, equationId: String) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val reference = referenceRepository.getById(referenceId)
                val equation = equationRepository.getEquationById(equationId)

                if (reference != null && equation != null) {
                    reference.equationBEE = equation
                    referenceRepository.update(reference)
                    _currentReferenceId.value = referenceId
                    _operationMessage.value = "Équation d'énergie basale mise à jour avec succès"
                } else {
                    _operationMessage.value = "Référence ou équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour l'équation de besoin énergétique de base pour la référence courante avec
     * l'équation fournie.
     */
    fun setEquationBEE(equation: Equation?) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val referenceId = _currentReferenceId.value
                if (referenceId.isNullOrEmpty()) {
                    _operationMessage.value = "Aucune référence sélectionnée"
                    return@launch
                }

                if (equation == null) {
                    val reference = referenceRepository.getById(referenceId)
                    if (reference != null) {
                        reference.equationBEE = Equation()
                        referenceRepository.update(reference)
                        _equationBEE.value = null
                        _operationMessage.value = "Équation d'énergie basale supprimée"
                    } else {
                        _operationMessage.value = "Référence non trouvée"
                    }
                } else {
                    val success = referenceRepository.updateEquationBEE(referenceId, equation)
                    if (success) {
                        _equationBEE.value = equation
                        _operationMessage.value =
                                "Équation d'énergie basale mise à jour avec succès"
                    } else {
                        _operationMessage.value = "Échec de la mise à jour de l'équation"
                    }
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Met à jour l'équation d'énergie digestible commerciale pour la référence spécifiée. */
    fun setEquationDEcom(referenceId: String, equationId: String) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val reference = referenceRepository.getById(referenceId)
                val equation = equationRepository.getEquationById(equationId)

                if (reference != null && equation != null) {
                    reference.equationDEcom = equation
                    referenceRepository.update(reference)
                    _currentReferenceId.value = referenceId
                    _operationMessage.value = "Équation DE commerciale mise à jour avec succès"
                } else {
                    _operationMessage.value = "Référence ou équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour l'équation d'énergie métabolisable pour la référence courante avec l'équation
     * fournie.
     */
    fun setEquationME(equation: Equation?) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val referenceId = _currentReferenceId.value
                if (referenceId.isNullOrEmpty()) {
                    _operationMessage.value = "Aucune référence sélectionnée"
                    return@launch
                }

                if (equation == null) {
                    val reference = referenceRepository.getById(referenceId)
                    if (reference != null) {
                        reference.equationME = Equation()
                        referenceRepository.update(reference)
                        _equationME.value = null
                        _operationMessage.value = "Équation d'énergie métabolisable supprimée"
                    } else {
                        _operationMessage.value = "Référence non trouvée"
                    }
                } else {
                    val success = referenceRepository.updateEquationME(referenceId, equation)
                    if (success) {
                        _equationME.value = equation
                        _operationMessage.value =
                                "Équation d'énergie métabolisable mise à jour avec succès"
                    } else {
                        _operationMessage.value = "Échec de la mise à jour de l'équation"
                    }
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Met à jour l'équation d'énergie digestible brute pour la référence spécifiée. */
    fun setEquationDEraw(referenceId: String, equationId: String) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val reference = referenceRepository.getById(referenceId)
                val equation = equationRepository.getEquationById(equationId)

                if (reference != null && equation != null) {
                    reference.equationDEraw = equation
                    referenceRepository.update(reference)
                    _currentReferenceId.value = referenceId
                    _operationMessage.value = "Équation DE brute mise à jour avec succès"
                } else {
                    _operationMessage.value = "Référence ou équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour l'équation d'énergie digestible commerciale pour la référence courante avec
     * l'équation fournie.
     */
    fun setEquationDEcom(equation: Equation?) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val referenceId = _currentReferenceId.value
                if (referenceId.isNullOrEmpty()) {
                    _operationMessage.value = "Aucune référence sélectionnée"
                    return@launch
                }

                if (equation == null) {
                    val reference = referenceRepository.getById(referenceId)
                    if (reference != null) {
                        reference.equationDEcom = Equation()
                        referenceRepository.update(reference)
                        _equationDEcom.value = null
                        _operationMessage.value =
                                "Équation d'énergie digestible commerciale supprimée"
                    } else {
                        _operationMessage.value = "Référence non trouvée"
                    }
                } else {
                    val success = referenceRepository.updateEquationDEcom(referenceId, equation)
                    if (success) {
                        _equationDEcom.value = equation
                        _operationMessage.value =
                                "Équation d'énergie digestible commerciale mise à jour avec succès"
                    } else {
                        _operationMessage.value = "Échec de la mise à jour de l'équation"
                    }
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Met à jour l'équation d'énergie digestible brute pour la référence courante avec l'équation
     * fournie.
     */
    fun setEquationDEraw(equation: Equation?) {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            _operationMessage.value = null

            try {
                val referenceId = _currentReferenceId.value
                if (referenceId.isNullOrEmpty()) {
                    _operationMessage.value = "Aucune référence sélectionnée"
                    return@launch
                }

                if (equation == null) {
                    val reference = referenceRepository.getById(referenceId)
                    if (reference != null) {
                        reference.equationDEraw = Equation()
                        referenceRepository.update(reference)
                        _equationDEraw.value = null
                        _operationMessage.value = "Équation d'énergie digestible brute supprimée"
                    } else {
                        _operationMessage.value = "Référence non trouvée"
                    }
                } else {
                    val success = referenceRepository.updateEquationDEraw(referenceId, equation)
                    if (success) {
                        _equationDEraw.value = equation
                        _operationMessage.value =
                                "Équation d'énergie digestible brute mise à jour avec succès"
                    } else {
                        _operationMessage.value = "Échec de la mise à jour de l'équation"
                    }
                }
            } catch (e: Exception) {
                _operationMessage.value =
                        "Erreur lors de la mise à jour de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
