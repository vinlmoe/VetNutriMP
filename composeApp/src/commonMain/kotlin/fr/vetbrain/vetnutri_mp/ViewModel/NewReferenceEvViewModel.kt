package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
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
 * ViewModel pour l'édition et la création de références nutritionnelles (ReferenceEv).
 *
 * @param repository Le repository pour les opérations sur les références
 * @param equationRepository Le repository pour les opérations sur les équations
 * @param biblioRefRepository Le repository pour les opérations sur les références bibliographiques
 */
class NewReferenceEvViewModel(
        private val repository: DatabaseReferenceEvRepository,
        private val equationRepository: EquationRepository? = null,
        private val biblioRefRepository: BiblioRefRepository? = null
) {

    // État pour la référence en cours d'édition
    private val _currentReference = MutableStateFlow(ReferenceEv())
    val currentReference: StateFlow<ReferenceEv> = _currentReference.asStateFlow()

    // État pour les équations disponibles
    private val _availableEquations = MutableStateFlow<List<Equation>>(emptyList())
    val availableEquations: StateFlow<List<Equation>> = _availableEquations.asStateFlow()

    // État pour les références bibliographiques disponibles
    private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs

    // État pour les nutriments définis
    private val _definedNutrients =
            MutableStateFlow<Map<Nutrient, Map<Reflevel, Float>>>(emptyMap())
    val definedNutrients: StateFlow<Map<Nutrient, Map<Reflevel, Float>>> =
            _definedNutrients.asStateFlow()

    // État pour les erreurs
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // État pour le succès de l'opération
    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    // État pour indiquer si nous sommes en mode édition ou création
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // État pour indiquer si nous sommes en cours de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État pour les équations
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())
    val equations: StateFlow<List<Equation>> = _equations.asStateFlow()

    // État pour les erreurs de chargement des équations
    private val _equationsError = MutableStateFlow<String?>(null)
    val equationsError: StateFlow<String?> = _equationsError.asStateFlow()

    // Exposer la référence courante en tant qu'équations courantes pour l'onglet équations
    val currentEquations: StateFlow<ReferenceEv> = _currentReference.asStateFlow()

    // Scope pour les coroutines
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    /** Initialise le ViewModel pour une nouvelle référence */
    fun initForNew() {
        _currentReference.value = ReferenceEv()
        _isEditMode.value = false
        _errorMessage.value = null
        _operationSuccess.value = false
        loadEquations()
        loadBiblioRefs()
    }

    /**
     * Initialise le ViewModel pour l'édition d'une référence existante
     *
     * @param referenceId L'identifiant de la référence à éditer
     */
    fun initForEdit(referenceId: String) {
        if (referenceId.isEmpty()) {
            initForNew()
            return
        }

        coroutineScope.launch {
            val reference = repository.getById(referenceId)
            if (reference != null) {
                _currentReference.value = reference
                _isEditMode.value = true
                updateDefinedNutrients(reference)

                // Charger les équations associées depuis la base de données
                loadEquationsForReference(reference)
            } else {
                _errorMessage.value = "Référence non trouvée"
                _currentReference.value = ReferenceEv()
                _isEditMode.value = false
            }
            loadEquations()
            loadBiblioRefs()
        }
    }

    /** Charge les équations disponibles dans le repository. */
    fun loadEquations() {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            try {
                val repo = equationRepository
                if (repo == null) {
                    println("Aucun repository d'équations disponible")
                    _equations.value = emptyList()
                    _availableEquations.value = emptyList()
                } else {
                    val equations = repo.getAllEquations()
                    _equations.value = equations
                    _availableEquations.value = equations
                    println("Nombre d'équations disponibles: ${equations.size}")
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement des équations: ${e.message}")
                _equations.value = emptyList()
                _availableEquations.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Charge les références bibliographiques disponibles. */
    fun loadBiblioRefs() {
        coroutineScope.launch(AppDispatchers.IO) {
            try {
                biblioRefRepository?.let { repo ->
                    repo.getAllBiblioRefs().collect { biblioRefs ->
                        _availableBiblioRefs.value = biblioRefs
                        println(
                                "Nombre de références bibliographiques chargées: ${biblioRefs.size}"
                        )
                    }
                }
                        ?: run {
                            println("Aucun repository de références bibliographiques disponible")
                            _availableBiblioRefs.value = emptyList()
                        }
            } catch (e: Exception) {
                println("Erreur lors du chargement des références bibliographiques: ${e.message}")
                _availableBiblioRefs.value = emptyList()
            }
        }
    }

    /**
     * Met à jour les nutriments définis pour la référence
     *
     * @param reference La référence dont les nutriments doivent être mis à jour
     */
    private fun updateDefinedNutrients(reference: ReferenceEv) {
        // Pour l'instant, nous allons simplement initialiser la map vide
        // et la remplir plus tard quand nous aurons une meilleure compréhension
        // de la structure des Nutrient
        _definedNutrients.value = emptyMap()
    }

    /**
     * Met à jour une propriété de la référence
     *
     * @param propertyName Le nom de la propriété à mettre à jour
     * @param value La nouvelle valeur de la propriété
     */
    fun updateReferenceProperty(propertyName: String, value: Any) {
        val currentRef = _currentReference.value
        val updatedRef =
                when (propertyName) {
                    "nom" -> currentRef.copy(nom = value as String)
                    "description" -> currentRef.copy(description = value as String)
                    "maladie" -> currentRef.copy(maladie = value as Boolean)
                    "nomMaladie" -> currentRef.copy(nomMaladie = value as String)
                    "nomEnergie" -> currentRef.copy(nomEnergie = value as String)
                    "consistent" -> currentRef.copy(consistent = value as Int)
                    "espece" -> currentRef.copy(espece = value as Espece)
                    "stadePhysio" -> currentRef.copy(stadePhysio = value as StadePhysio)
                    else -> currentRef
                }
        _currentReference.value = updatedRef
    }

    /**
     * Met à jour la valeur d'un nutriment pour un niveau de référence spécifique.
     *
     * @param nutrient Le nutriment à mettre à jour
     * @param value La valeur à définir
     * @param level Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
     * @param unit L'unité de la valeur
     * @param biblioRef La référence bibliographique associée
     */
    fun updateNutrientValue(
            nutrient: Nutrient,
            value: Float,
            level: Reflevel,
            unit: UnitReqEnum,
            biblioRef: BiblioRef
    ) {
        val reference = _currentReference.value

        if (value >= 0) {
            // Définir la valeur du nutriment
            reference.definirNutriment(value, nutrient, level, unit, biblioRef)
        } else {
            // Supprimer le nutriment si la valeur est négative
            reference.supprimerNutriment(nutrient, level)
        }

        // Mettre à jour la référence
        _currentReference.value = reference.copy()
    }

    /** Sauvegarde la référence dans le repository */
    fun saveReference() {
        val reference = _currentReference.value
        if (reference.nom.isBlank()) {
            _errorMessage.value = "Le nom de la référence est requis"
            return
        }

        coroutineScope.launch {
            try {
                val success =
                        if (_isEditMode.value) {
                            repository.update(reference)
                        } else {
                            repository.create(reference)
                        }

                if (success) {
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Erreur lors de la sauvegarde"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la sauvegarde: ${e.message}"
                _operationSuccess.value = false
            }
        }
    }

    /** Sauvegarde silencieuse sans déclencher la navigation automatique */
    fun saveReferenceSilently() {
        val reference = _currentReference.value
        if (reference.nom.isBlank()) {
            return // Ne pas sauvegarder si pas de nom
        }

        coroutineScope.launch {
            try {
                if (_isEditMode.value) {
                    repository.update(reference)
                } else {
                    repository.create(reference)
                }
                // Pas de mise à jour de _operationSuccess pour éviter la navigation automatique
            } catch (e: Exception) {
                // Log silencieux de l'erreur
                println("DEBUG: Erreur lors de la sauvegarde silencieuse: ${e.message}")
            }
        }
    }

    /** Vérifie s'il y a des modifications non sauvegardées */
    fun hasUnsavedChanges(): Boolean {
        val reference = _currentReference.value
        return reference.nom.isNotBlank() ||
                reference.description.isNotBlank() ||
                reference.nomEnergie.isNotBlank() ||
                reference.nomMaladie.isNotBlank()
    }

    /**
     * Associe une équation à la référence
     *
     * @param equation L'équation à associer
     * @param type Le type d'équation (BEE, BW, DEcom, DEraw)
     */
    fun setEquation(equation: Equation, type: String) {
        val reference = _currentReference.value
        when (type) {
            "BEE" -> reference.equationBEE = equation
            "BW" -> reference.equationBW = equation
            "DEcom" -> reference.equationDEcom = equation
            "DEraw" -> reference.equationDEraw = equation
            else -> reference.equationsNut.add(equation)
        }
        _currentReference.value = reference
    }

    /**
     * Définit l'équation de poids corporel pour la référence
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationBW(equation: Equation?) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationBW = equation
                val success = repository.update(reference)
                if (success) {
                    _currentReference.value = reference
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation de poids corporel"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Définit l'équation de besoin énergétique basal pour la référence
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationBEE(equation: Equation?) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationBEE = equation
                val success = repository.update(reference)
                if (success) {
                    _currentReference.value = reference
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation de besoin énergétique basal"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Définit l'équation d'énergie digestible pour aliments composés pour la référence
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationDEcom(equation: Equation?) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationDEcom = equation
                val success = repository.update(reference)
                if (success) {
                    _currentReference.value = reference
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation d'énergie digestible pour aliments composés"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Définit l'équation d'énergie digestible pour aliments bruts pour la référence
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationDEraw(equation: Equation?) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationDEraw = equation
                val success = repository.update(reference)
                if (success) {
                    _currentReference.value = reference
                    _operationSuccess.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation d'énergie digestible pour aliments bruts"
                    _operationSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
                _operationSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Méthodes silencieuses pour la sélection d'équations (sans déclencher operationSuccess)

    /**
     * Définit l'équation de poids corporel pour la référence sans déclencher la navigation
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationBWSilently(equation: Equation?) {
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationBW = equation
                repository.update(reference)
                _currentReference.value = reference
                // Pas de mise à jour de _operationSuccess pour éviter la navigation
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    /**
     * Définit l'équation de besoin énergétique basal pour la référence sans déclencher la
     * navigation
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationBEESilently(equation: Equation?) {
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationBEE = equation
                repository.update(reference)
                _currentReference.value = reference
                // Pas de mise à jour de _operationSuccess pour éviter la navigation
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    /**
     * Définit l'équation d'énergie digestible pour aliments composés sans déclencher la navigation
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationDEcomSilently(equation: Equation?) {
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationDEcom = equation
                repository.update(reference)
                _currentReference.value = reference
                // Pas de mise à jour de _operationSuccess pour éviter la navigation
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    /**
     * Définit l'équation d'énergie digestible pour aliments bruts sans déclencher la navigation
     *
     * @param equation L'équation à définir ou null pour supprimer l'équation
     */
    fun setEquationDErawSilently(equation: Equation?) {
        coroutineScope.launch {
            try {
                val reference = _currentReference.value
                reference.equationDEraw = equation
                repository.update(reference)
                _currentReference.value = reference
                // Pas de mise à jour de _operationSuccess pour éviter la navigation
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
            }
        }
    }

    /** Efface les messages d'erreur */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /** Réinitialise l'état de succès de l'opération */
    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }

    /** Charge les équations associées à une référence depuis la base de données */
    private suspend fun loadEquationsForReference(reference: ReferenceEv) {
        try {
            val repo = equationRepository ?: return

            // Charger les équations par UUID si elles existent
            reference.equationBW?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationBW = loadedEquation
                    println("DEBUG: Équation BW chargée: ${loadedEquation.name}")
                }
            }

            reference.equationBEE?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationBEE = loadedEquation
                    println("DEBUG: Équation BEE chargée: ${loadedEquation.name}")
                }
            }

            reference.equationDEcom?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationDEcom = loadedEquation
                    println("DEBUG: Équation DEcom chargée: ${loadedEquation.name}")
                }
            }

            reference.equationDEraw?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationDEraw = loadedEquation
                    println("DEBUG: Équation DEraw chargée: ${loadedEquation.name}")
                }
            }

            // Mettre à jour la référence avec les équations chargées
            _currentReference.value = reference
        } catch (e: Exception) {
            println(
                    "DEBUG: Erreur lors du chargement des équations pour la référence: ${e.message}"
            )
        }
    }
}
