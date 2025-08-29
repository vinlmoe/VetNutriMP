package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import fr.vetbrain.vetnutri_mp.Utils.ResultatValidation
import fr.vetbrain.vetnutri_mp.Utils.TypeEquationValidation
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    // Variables reconnues (calculées dynamiquement à partir du script)
    val recognizedVariables: StateFlow<List<String>> =
            currentEquation
                    .map { equation ->
                        if (equation.equationScript.isBlank()) {
                            emptyList()
                        } else {
                            val variablesUtilisees =
                                    ExpressionEvaluator.extraireVariables(equation.equationScript)
                            variablesUtilisees.mapNotNull { variable ->
                                getVariableDisplayName(variable)
                            }
                        }
                    }
                    .stateIn(
                            scope = viewModelScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            initialValue = emptyList()
                    )

    /** Obtient le nom d'affichage d'une variable sous le format "label (displayName)" */
    private fun getVariableDisplayName(variable: String): String? {
        // Vérifier si c'est une variable VariableKind
        val variableKind = VariableKind.entries.find { it.label == variable }
        if (variableKind != null) {
            return "${variableKind.translateEnum()} (${variableKind.dup})"
        }

        // Vérifier si c'est un nutriment et obtenir son nom d'affichage
        val nutrientMain =
                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries.find { it.label == variable }
        if (nutrientMain != null) {
            return "${nutrientMain.translateEnum()} (${nutrientMain.nameToString()})"
        }

        // Pour les autres nutriments, utiliser le label comme nom d'affichage
        if (fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries.any { it.label == variable } ||
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries.any {
                            it.label == variable
                        } ||
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries.any {
                            it.label == variable
                        } ||
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries.any {
                            it.label == variable
                        } ||
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.entries.any {
                            it.label == variable
                        } ||
                        fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries.any { it.label == variable }
        ) {
            return "${variable} (${variable})"
        }

        // Variables système avec leurs descriptions
        return when (variable) {
            else -> {
                // Si c'est une variable reconnue par NutrientResolver mais pas trouvée ci-dessus
                if (NutrientResolver.isRecognizedLabel(variable)) {
                    "${variable} (Variable reconnue)"
                } else {
                    null
                }
            }
        }
    }

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
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement des équations: ${e.message}"
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
            } catch (e: Exception) {}
        }
    }

    /** Duplique une équation (tous les champs conservés, nouvel UUID) et l'enregistre */
    fun duplicateEquation(source: Equation) {
        coroutineScope.launch(AppDispatchers.IO) {
            try {
                // Créer une duplication complète de l'équation avec un nouvel UUID
                val duplicated = source.copy(
                    uuid = genUUID(),
                    name = source.name + " (Copie)",
                    // Conserver tous les autres champs :
                    description = source.description,
                    equationScript = source.equationScript,
                    bib = source.bib, // Garder la même référence bibliographique
                    specie = source.specie,
                    kind = source.kind,
                    nutrient = source.nutrient,
                    consistent = source.consistent,
                    variables = source.variables.toMutableList(), // Copier la liste des variables
                    correctionFactor = source.correctionFactor,
                    ratio = source.ratio,
                    creationDate = source.creationDate, // Garder la date de création originale
                    lastUpdate = source.lastUpdate // Garder la dernière mise à jour originale
                )

                equationRepository.saveEquation(duplicated)
                loadEquations()
                _operationMessage.value = "Équation dupliquée avec succès"
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de la duplication: ${e.message}"
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

    /** Met à jour le flag ratio de l'équation */
    fun updateRatio(ratio: Boolean) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(ratio = ratio)
    }

    /** Analyse le script pour détecter et gérer automatiquement les variables */
    private fun analyzeScriptForVariables(script: String) {
        // Utiliser le nouveau parser pour extraire les variables
        val variablesUtilisees = ExpressionEvaluator.extraireVariables(script)

        // Récupérer toutes les variables VariableKind possibles
        val allVariableKinds = VariableKind.entries

        // Créer une liste des variables détectées qui correspondent à VariableKind
        val detectedVariables = mutableListOf<VariableKind>()
        val unrecognizedVars = mutableListOf<String>()

        // Classer les variables trouvées
        variablesUtilisees.forEach { variable ->
            val correspondingVariableKind = allVariableKinds.find { it.label == variable }
            if (correspondingVariableKind != null) {
                detectedVariables.add(correspondingVariableKind)
            } else {
                // Vérifier si c'est une variable de nutriment
                if (!isNutrientVariable(variable)) {
                    unrecognizedVars.add(variable)
                }
            }
        }

        // Mettre à jour la liste des variables de l'équation
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(variables = detectedVariables.toMutableList())

        // Mettre à jour la liste des variables non reconnues
        _unrecognizedVariables.value = unrecognizedVars
    }

    /** Vérifie si une variable est un nutriment connu */
    private fun isNutrientVariable(variable: String): Boolean {
        val nutrientsMain = NutrientMain.entries.map { it.label }
        val nutrientsLipides = NutrientLipid.entries.map { it.label }
        val nutrientsVitamines = NutrientVitam.entries.map { it.label }
        val nutrientsMacro = NutrientMacro.entries.map { it.label }
        val nutrientsMin = NutrientMin.entries.map { it.label }
        val acideAmines = AAEnum.entries.map { it.label }

        return variable in
                (nutrientsMain +
                        nutrientsLipides +
                        nutrientsVitamines +
                        nutrientsMacro +
                        nutrientsMin +
                        acideAmines)
    }

    /** Convertit un EquationKind en TypeEquationValidation */
    private fun mapEquationKindToValidationType(kind: EquationKind): TypeEquationValidation {
        return when (kind) {
            EquationKind.ENERGYNEED -> TypeEquationValidation.BESOIN_ENERGETIQUE
            EquationKind.MW -> TypeEquationValidation.BESOIN_ENERGETIQUE
            EquationKind.NEED -> TypeEquationValidation.BESOIN_NUTRITIONNEL
            EquationKind.ENERGYDENSITY -> TypeEquationValidation.DENSITE_ENERGETIQUE
            EquationKind.COMPLEMENTARY_NUTRIENT -> TypeEquationValidation.DENSITE_ENERGETIQUE
            else -> TypeEquationValidation.GENERALE
        }
    }

    /** Valide l'expression mathématique de l'équation courante */
    fun validerExpressionCourante(): ResultatValidation {
        val equation = _currentEquation.value
        val typeValidation = mapEquationKindToValidationType(equation.kind)
        return EquationEvaluator.validerExpression(equation.equationScript, typeValidation)
    }

    /** Teste l'expression courante avec des valeurs d'exemple */
    fun testerExpressionCourante(): Double? {
        val equation = _currentEquation.value
        val typeValidation = mapEquationKindToValidationType(equation.kind)
        return EquationEvaluator.testerExpression(equation.equationScript, typeValidation)
    }

    /** Obtient la liste de toutes les variables disponibles selon le type d'équation */
    fun getVariablesDisponibles(): Set<String> {
        val equation = _currentEquation.value
        val typeValidation = mapEquationKindToValidationType(equation.kind)

        return when (typeValidation) {
            TypeEquationValidation.BESOIN_ENERGETIQUE -> EquationEvaluator.toutesLesVariables
            TypeEquationValidation.BESOIN_NUTRITIONNEL ->
                    EquationEvaluator.toutesLesVariables + getNutrientsVariables()
            TypeEquationValidation.DENSITE_ENERGETIQUE -> getNutrientsVariables()
            TypeEquationValidation.GENERALE ->
                    EquationEvaluator.toutesLesVariables + getNutrientsVariables()
        }
    }

    /** Obtient la liste des variables de nutriments disponibles */
    private fun getNutrientsVariables(): Set<String> {
        val nutrientsMain = NutrientMain.entries.map { it.label }
        val nutrientsLipides = NutrientLipid.entries.map { it.label }
        val nutrientsVitamines = NutrientVitam.entries.map { it.label }
        val nutrientsMacro = NutrientMacro.entries.map { it.label }
        val nutrientsMin = NutrientMin.entries.map { it.label }
        val acideAmines = AAEnum.entries.map { it.label }

        return (nutrientsMain +
                        nutrientsLipides +
                        nutrientsVitamines +
                        nutrientsMacro +
                        nutrientsMin +
                        acideAmines)
                .toSet()
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

    /** Met à jour l'espèce d'application de l'équation */
    fun updateSpecie(specie: Espece?) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(specie = specie)
    }

    /**
     * Met à jour le nutriment associé à l'équation (pour les types NEED et COMPLEMENTARY_NUTRIENT)
     */
    fun updateNutrient(nutrient: Nutrient?) {
        val currentValue = _currentEquation.value
        _currentEquation.value = currentValue.copy(nutrient = nutrient)
    }

    /** Vérifie si le type d'équation actuel nécessite un nutriment */
    fun isNutrientRequired(): Boolean {
        val currentValue = _currentEquation.value
        return currentValue.kind == EquationKind.NEED ||
                currentValue.kind == EquationKind.COMPLEMENTARY_NUTRIENT
    }

    /** Obtient la liste de tous les nutriments disponibles */
    fun getAllNutrients(): List<Nutrient> {
        val nutrientsMain = NutrientMain.entries.toList()
        val nutrientsLipides = NutrientLipid.entries.toList()
        val nutrientsVitamines = NutrientVitam.entries.toList()
        val nutrientsMacro = NutrientMacro.entries.toList()
        val nutrientsMin = NutrientMin.entries.toList()
        val acideAmines = AAEnum.entries.toList()
        val nutrientAnalysis = NutrientAnalysis.entries.toList()

        return nutrientsMain +
                nutrientsLipides +
                nutrientsVitamines +
                nutrientsMacro +
                nutrientsMin +
                nutrientAnalysis +
                acideAmines
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

        // Valider la cohérence de l'équation
        val isConsistent = validerCoherenceEquation(equation.equationScript)

        // Mettre à jour le champ consistent de l'équation
        val equationToSave = equation.copy(consistent = isConsistent)
        _currentEquation.value = equationToSave

        coroutineScope.launch {
            _isLoading.value = true
            try {
                equationRepository.saveEquation(equationToSave)
                _saveSuccessful.value = true
                _operationMessage.value =
                        if (isConsistent) {
                            "Équation sauvegardée avec succès"
                        } else {
                            "Équation sauvegardée avec des variables non reconnues"
                        }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de la sauvegarde: ${e.message}"
                _saveSuccessful.value = false
            } finally {
                _isLoading.value = false
            }
        }

        return true
    }

    /**
     * Valide la cohérence d'une équation en vérifiant que toutes les variables sont reconnues et
     * que l'expression est évaluable
     */
    private fun validerCoherenceEquation(script: String): Boolean {
        if (script.isBlank()) {
            return false
        }

        try {
            // Extraire les variables de l'expression
            val variablesInExpression = ExpressionEvaluator.extraireVariables(script)

            // Vérifier les références circulaires selon le type d'équation
            val equation = _currentEquation.value
            val hasCircularReference =
                    when (equation.kind) {
                        EquationKind.ENERGYNEED -> {
                            // Interdire BE et BEE dans les équations de besoin énergétique
                            val forbiddenVars =
                                    variablesInExpression.filter { it in setOf("BE", "BEE") }
                            if (forbiddenVars.isNotEmpty()) {
                                _operationMessage.value =
                                        "Erreur: Une équation de besoin énergétique ne peut pas utiliser les variables BE ou BEE (référence circulaire)"
                                true
                            } else false
                        }
                        EquationKind.MW -> {
                            // Interdire MW dans les équations de poids métabolique
                            val forbiddenVars = variablesInExpression.filter { it == "MW" }
                            if (forbiddenVars.isNotEmpty()) {
                                _operationMessage.value =
                                        "Erreur: Une équation de poids métabolique ne peut pas utiliser la variable MW (référence circulaire)"
                                true
                            } else false
                        }
                        else -> false
                    }

            if (hasCircularReference) {
                return false
            }

            // Vérifier que toutes les variables sont reconnues
            val unrecognizedVariables =
                    variablesInExpression.filter { variable ->
                        // Vérifier si la variable est reconnue dans VariableKind
                        VariableKind.values().none { it.label == variable } &&
                                // Vérifier si c'est une variable de nutriment
                                !isNutrientVariable(variable)
                    }

            if (unrecognizedVariables.isNotEmpty()) {
                return false
            }

            // Tester l'évaluabilité de l'expression avec des valeurs par défaut
            val testVariables = mutableMapOf<String, Double>()
            variablesInExpression.forEach { variable ->
                testVariables[variable] =
                        when (variable) {
                            "BW" -> 25.0
                            "BEE" -> 400.0
                            "MW" -> 15.0
                            "iBW" -> 20.0
                            "AW" -> 30.0
                            "L" -> 6.0
                            "wG" -> 8.0
                            "wL" -> 4.0
                            else -> {
                                // Pour les variables de nutriments, utiliser une valeur par défaut
                                if (isNutrientVariable(variable)) {
                                    10.0
                                } else {
                                    // Pour les variables VariableKind
                                    1.0
                                }
                            }
                        }
            }

            // Tenter d'évaluer l'expression
            val result = ExpressionEvaluator.evaluer(script, testVariables)
            val isEvaluable = result != null && !result.isNaN() && result.isFinite()

            return isEvaluable
        } catch (e: Exception) {
            return false
        }
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

    /** Supprime une équation par son identifiant */
    fun deleteEquationById(equationId: String) {
        if (equationId.isEmpty()) {
            return
        }

        coroutineScope.launch {
            _isLoading.value = true
            try {
                equationRepository.deleteEquation(equationId)
                _operationMessage.value = "Équation supprimée avec succès"
                _saveSuccessful.value = true
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
