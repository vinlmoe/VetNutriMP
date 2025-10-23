package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
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
import kotlinx.datetime.Clock

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

    // État pour forcer la recomposition
    private val _forceUpdate = MutableStateFlow(0L)
    val forceUpdate: StateFlow<Long> = _forceUpdate.asStateFlow()

    // État pour les équations disponibles
    private val _availableEquations = MutableStateFlow<List<Equation>>(emptyList())
    val availableEquations: StateFlow<List<Equation>> = _availableEquations.asStateFlow()

    // État pour les références bibliographiques disponibles
    private val _availableBiblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val availableBiblioRefs: StateFlow<List<BiblioRef>> = _availableBiblioRefs

    // État pour les nutriments définis
    private val _definedNutrients =
            MutableStateFlow<Map<Nutrient, Map<Reflevel, Double>>>(emptyMap())
    val definedNutrients: StateFlow<Map<Nutrient, Map<Reflevel, Double>>> =
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

    // États séparés pour les listes de coefficients pour une réactivité optimale
    private val _coefficientsK1 =
            MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
    val coefficientsK1: StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> =
            _coefficientsK1.asStateFlow()

    private val _coefficientsK2 =
            MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
    val coefficientsK2: StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> =
            _coefficientsK2.asStateFlow()

    private val _coefficientsK3 =
            MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
    val coefficientsK3: StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> =
            _coefficientsK3.asStateFlow()

    private val _coefficientsK4 =
            MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
    val coefficientsK4: StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> =
            _coefficientsK4.asStateFlow()

    private val _coefficientsK5 =
            MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
    val coefficientsK5: StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> =
            _coefficientsK5.asStateFlow()

    // États pour les noms des groupes de coefficients
    private val _groupNames = MutableStateFlow(listOf("", "", "", "", ""))
    val groupNames: StateFlow<List<String>> = _groupNames.asStateFlow()

    // Scope pour les coroutines
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    /** Initialise le ViewModel pour une nouvelle référence */
    fun initForNew() {
        _currentReference.value = ReferenceEv()
        _isEditMode.value = false
        _errorMessage.value = null
        _operationSuccess.value = false
        syncCoefficientsFromReference()
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
                syncCoefficientsFromReference()

                // Charger les équations associées depuis la base de données
                loadEquationsForReference(reference)
            } else {
                _errorMessage.value = "Référence non trouvée"
                _currentReference.value = ReferenceEv()
                _isEditMode.value = false
                syncCoefficientsFromReference()
            }
            loadEquations()
            loadBiblioRefs()
        }
    }

    /** Synchronise les StateFlow des coefficients avec la référence actuelle */
    private fun syncCoefficientsFromReference() {
        val reference = _currentReference.value

        // Assainir les doublons éventuels (même nom et même valeur)
        reference.deduplicateCoefficients()

        // Synchroniser les listes de coefficients
        _coefficientsK1.value = reference.modk1.toList()
        _coefficientsK2.value = reference.modk2.toList()
        _coefficientsK3.value = reference.modk3.toList()
        _coefficientsK4.value = reference.modk4.toList()
        _coefficientsK5.value = reference.modk5.toList()

        // Synchroniser les noms des groupes
        _groupNames.value =
                listOf(
                        reference.nomk1,
                        reference.nomk2,
                        reference.nomk3,
                        reference.nomk4,
                        reference.nomk5
                )
    }

    /** Charge les équations disponibles dans le repository. */
    fun loadEquations() {
        coroutineScope.launch(AppDispatchers.IO) {
            _isLoading.value = true
            try {
                val repo = equationRepository
                if (repo == null) {
                    _equations.value = emptyList()
                    _availableEquations.value = emptyList()
                } else {
                    val equations = repo.getAllEquations()
                    _equations.value = equations
                    _availableEquations.value = equations
                }
            } catch (e: Exception) {
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
                    }
                }
                        ?: run { _availableBiblioRefs.value = emptyList() }
            } catch (e: Exception) {
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


        // SOLUTION HYBRIDE: Utiliser copy() avec les paramètres du constructeur primaire seulement
        // et préserver manuellement les données complexes
        val updatedRef = when (propertyName) {
            "nom" -> {
                currentRef.copy(nom = value as String)
            }
            "description" -> currentRef.copy(description = value as String)
            "maladie" -> currentRef.copy(maladie = value as Boolean)
            "nomMaladie" -> currentRef.copy(nomMaladie = value as String)
            "nomEnergie" -> currentRef.copy(nomEnergie = value as String)
            "consistent" -> currentRef.copy(consistent = value as Int)
            "espece" -> currentRef.copy(espece = value as Espece)
            "stadePhysio" -> currentRef.copy(stadePhysio = value as StadePhysio)
            else -> currentRef
        }

        // PRÉSERVER LES DONNÉES COMPLEXES après copy() pour TOUS les cas
        // Restaurer les données complexes depuis l'original
        updatedRef.equationBW = currentRef.equationBW
        updatedRef.equationBEE = currentRef.equationBEE
        updatedRef.equationDEcom = currentRef.equationDEcom
        updatedRef.equationDEraw = currentRef.equationDEraw
        updatedRef.equationME = currentRef.equationME
        updatedRef.equationsNut = currentRef.equationsNut

        // Copier les coefficients (MutableList)
        updatedRef.modk1.clear()
        updatedRef.modk1.addAll(currentRef.modk1)
        updatedRef.modk2.clear()
        updatedRef.modk2.addAll(currentRef.modk2)
        updatedRef.modk3.clear()
        updatedRef.modk3.addAll(currentRef.modk3)
        updatedRef.modk4.clear()
        updatedRef.modk4.addAll(currentRef.modk4)
        updatedRef.modk5.clear()
        updatedRef.modk5.addAll(currentRef.modk5)

        // Copier les noms des coefficients
        updatedRef.nomk1 = currentRef.nomk1
        updatedRef.nomk2 = currentRef.nomk2
        updatedRef.nomk3 = currentRef.nomk3
        updatedRef.nomk4 = currentRef.nomk4
        updatedRef.nomk5 = currentRef.nomk5

        // Copier les nutriments (MutableMap)
        updatedRef.getRefMapMin().clear()
        updatedRef.getRefMapMin().putAll(currentRef.getRefMapMin())
        updatedRef.getRefMapMax().clear()
        updatedRef.getRefMapMax().putAll(currentRef.getRefMapMax())
        updatedRef.getRefMapOMin().clear()
        updatedRef.getRefMapOMin().putAll(currentRef.getRefMapOMin())
        updatedRef.getRefMapOMax().clear()
        updatedRef.getRefMapOMax().putAll(currentRef.getRefMapOMax())

        _currentReference.value = updatedRef


    }

    /**
     * Met à jour la valeur d'un nutriment dans la référence
     *
     * @param nutrient Le nutriment à mettre à jour
     * @param value La valeur à définir
     * @param level Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
     * @param unit L'unité de la valeur
     * @param biblioRef La référence bibliographique associée
     * @param unitEnum L'unité physique personnalisée (optionnel)
     */
    fun updateNutrientValue(
            nutrient: Nutrient,
            value: Double,
            level: Reflevel,
            unit: UnitReqEnum,
            biblioRef: BiblioRef,
            unitEnum: UnitEnum? = null
    ) {
        val reference = _currentReference.value

        if (value >= 0) {
            // Définir la valeur du nutriment avec l'UnitEnum personnalisé ou par défaut
            reference.definirNutriment(
                    value,
                    nutrient,
                    level,
                    unit,
                    biblioRef,
                    unitEnum ?: nutrient.ue
            )
        } else {
            // Supprimer le nutriment si la valeur est négative
            reference.supprimerNutriment(nutrient, level)
        }

        // Forcer la recomposition en assignant une nouvelle référence
        _currentReference.value = reference

        // Déclencher un timestamp de mise à jour forcée
        _forceUpdate.value = Clock.System.now().toEpochMilliseconds()
    }

    /**
     * Supprime un nutriment à un niveau donné
     *
     * @param nutrient Le nutriment à supprimer
     * @param level Le niveau de référence (MIN, MAX, OPTIMIN, OPTIMAX)
     */
    fun removeNutrientValue(nutrient: Nutrient, level: Reflevel) {
        val reference = _currentReference.value
        reference.supprimerNutriment(nutrient, level)

        // Forcer la recomposition en assignant une nouvelle référence
        _currentReference.value = reference

        // Déclencher un timestamp de mise à jour forcée
        _forceUpdate.value = Clock.System.now().toEpochMilliseconds()
    }

    /** Force la mise à jour du StateFlow pour déclencher la recomposition */
    private fun updateReferenceStateFlow() {
        // Déclencher un timestamp de mise à jour forcée
        _forceUpdate.value = Clock.System.now().toEpochMilliseconds()
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

    /**
     * Retourne les équations de type nutriment complémentaire filtrées par espèce de la référence
     * et incluant celles définies pour l'espèce générique CH.
     */
    fun getComplementaryEquationsForCurrent(): List<Equation> {
        val all: List<Equation> = _availableEquations.value
        val specie: Espece = _currentReference.value.espece
        return all.filter {
            it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.COMPLEMENTARY_NUTRIENT &&
                    (it.specie == specie || it.specie == Espece.CH)
        }
    }

    /** Indique si une équation complémentaire est déjà sélectionnée sur la référence. */
    fun isComplementaryEquationSelected(equation: Equation): Boolean {
        return _currentReference.value.equationsNut.any { it.uuid == equation.uuid }
    }

    /** Ajoute/retire une équation complémentaire à la référence et persiste. */
    fun toggleComplementaryEquation(equation: Equation) {
        coroutineScope.launch {
            try {
                val current = _currentReference.value
                val already = current.equationsNut.any { it.uuid == equation.uuid }
                if (already) {
                    current.equationsNut =
                            current.equationsNut.filter { it.uuid != equation.uuid }.toMutableList()
                } else {
                    val newList = current.equationsNut.toMutableList()
                    newList.add(equation)
                    current.equationsNut = newList
                }
                val ok: Boolean = repository.update(current)
                if (ok) {
                    // Recharge depuis la base pour obtenir une nouvelle instance et déclencher la
                    // recomposition
                    val reloaded = repository.getById(current.uuid)
                    if (reloaded != null) {
                        _currentReference.value = reloaded
                    } else {
                        // À défaut, force un tick de mise à jour
                        _currentReference.value = current
                        _forceUpdate.value = Clock.System.now().toEpochMilliseconds()
                    }
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour des équations complémentaires"
                }
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
                }
            }

            reference.equationBEE?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationBEE = loadedEquation
                }
            }

            reference.equationDEcom?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationDEcom = loadedEquation
                }
            }

            reference.equationDEraw?.let { equation ->
                val loadedEquation = repo.getEquationById(equation.uuid)
                if (loadedEquation != null) {
                    reference.equationDEraw = loadedEquation
                }
            }

            // Mettre à jour la référence avec les équations chargées
            _currentReference.value = reference
        } catch (e: Exception) {}
    }

    // ==================== MÉTHODES POUR LA GESTION DES COEFFICIENTS ====================

    /**
     * Met à jour le nom d'un groupe de coefficients
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @param name Nouveau nom du groupe
     */
    fun updateCoefficientGroupName(groupIndex: Int, name: String) {
        val reference = _currentReference.value
        when (groupIndex) {
            0 -> reference.nomk1 = name
            1 -> reference.nomk2 = name
            2 -> reference.nomk3 = name
            3 -> reference.nomk4 = name
            4 -> reference.nomk5 = name
        }

        // Mettre à jour les StateFlow
        val newGroupNames = _groupNames.value.toMutableList()
        newGroupNames[groupIndex] = name
        _groupNames.value = newGroupNames

        // Pas besoin de .copy() car on modifie directement les StateFlow
        _currentReference.value = reference
    }

    /**
     * Récupère la liste des coefficients pour un groupe donné depuis le StateFlow
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @return Liste des coefficients
     */
    fun getCoefficientGroup(groupIndex: Int): List<fr.vetbrain.vetnutri_mp.Data.CoefP> {
        return when (groupIndex) {
            0 -> _coefficientsK1.value
            1 -> _coefficientsK2.value
            2 -> _coefficientsK3.value
            3 -> _coefficientsK4.value
            4 -> _coefficientsK5.value
            else -> emptyList()
        }
    }

    /**
     * Récupère le StateFlow de la liste des coefficients pour un groupe donné
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @return StateFlow de la liste des coefficients
     */
    fun getCoefficientGroupStateFlow(
            groupIndex: Int
    ): StateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>> {
        return when (groupIndex) {
            0 -> coefficientsK1
            1 -> coefficientsK2
            2 -> coefficientsK3
            3 -> coefficientsK4
            4 -> coefficientsK5
            else ->
                    MutableStateFlow<List<fr.vetbrain.vetnutri_mp.Data.CoefP>>(emptyList())
                            .asStateFlow()
        }
    }

    /**
     * Récupère le nom d'un groupe de coefficients
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @return Nom du groupe
     */
    fun getCoefficientGroupName(groupIndex: Int): String {
        return _groupNames.value.getOrElse(groupIndex) { "" }
    }

    /**
     * Ajoute un nouveau coefficient à un groupe
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @param description Description du coefficient
     * @param coef Valeur du coefficient
     */
    fun addCoefficient(groupIndex: Int, description: String, coef: Double) {
        val reference = _currentReference.value
        val newCoef =
                fr.vetbrain.vetnutri_mp.Data.CoefP(
                        description = description,
                        coef = coef,
                        groupUUID = groupIndex
                )

        // Ajouter dans la MutableList de ReferenceEv
        when (groupIndex) {
            0 -> reference.modk1.add(newCoef)
            1 -> reference.modk2.add(newCoef)
            2 -> reference.modk3.add(newCoef)
            3 -> reference.modk4.add(newCoef)
            4 -> reference.modk5.add(newCoef)
        }

        // Mettre à jour le StateFlow correspondant avec une nouvelle liste
        updateCoefficientStateFlow(groupIndex, reference)

        _currentReference.value = reference
    }

    /**
     * Supprime un coefficient d'un groupe
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @param coefficientIndex Index du coefficient dans le groupe
     */
    fun removeCoefficient(groupIndex: Int, coefficientIndex: Int) {
        val reference = _currentReference.value
        val group =
                when (groupIndex) {
                    0 -> reference.modk1
                    1 -> reference.modk2
                    2 -> reference.modk3
                    3 -> reference.modk4
                    4 -> reference.modk5
                    else -> return
                }

        if (coefficientIndex >= 0 && coefficientIndex < group.size) {
            group.removeAt(coefficientIndex)

            // Mettre à jour le StateFlow correspondant avec une nouvelle liste
            updateCoefficientStateFlow(groupIndex, reference)

            _currentReference.value = reference
        }
    }

    /**
     * Met à jour un coefficient existant
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @param coefficientIndex Index du coefficient dans le groupe
     * @param description Nouvelle description
     * @param coef Nouvelle valeur
     */
    fun updateCoefficient(
            groupIndex: Int,
            coefficientIndex: Int,
            description: String,
            coef: Double
    ) {
        val reference = _currentReference.value
        val group =
                when (groupIndex) {
                    0 -> reference.modk1
                    1 -> reference.modk2
                    2 -> reference.modk3
                    3 -> reference.modk4
                    4 -> reference.modk5
                    else -> return
                }

        if (coefficientIndex >= 0 && coefficientIndex < group.size) {
            val coefficient = group[coefficientIndex]
            coefficient.description = description
            coefficient.coef = coef

            // Mettre à jour le StateFlow correspondant avec une nouvelle liste
            updateCoefficientStateFlow(groupIndex, reference)

            _currentReference.value = reference
        }
    }

    /**
     * Met à jour le StateFlow d'un groupe de coefficients spécifique
     *
     * @param groupIndex Index du groupe (0-4 pour k1-k5)
     * @param reference Référence actuelle
     */
    private fun updateCoefficientStateFlow(groupIndex: Int, reference: ReferenceEv) {
        when (groupIndex) {
            0 -> _coefficientsK1.value = reference.modk1.toList()
            1 -> _coefficientsK2.value = reference.modk2.toList()
            2 -> _coefficientsK3.value = reference.modk3.toList()
            3 -> _coefficientsK4.value = reference.modk4.toList()
            4 -> _coefficientsK5.value = reference.modk5.toList()
        }
    }
}
