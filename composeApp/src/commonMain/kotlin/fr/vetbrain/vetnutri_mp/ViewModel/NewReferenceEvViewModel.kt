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
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.ReferenceEvRepository
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
        private val repository: ReferenceEvRepository,
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

    /** Efface les messages d'erreur */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /** Réinitialise l'état de succès de l'opération */
    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }

    /**
     * Met à jour le nom d'un coefficient
     *
     * @param coefIndex L'index du coefficient (1 à 5)
     * @param name Le nouveau nom du coefficient
     */
    fun updateCoefName(coefIndex: Int, name: String) {
        val reference = _currentReference.value
        when (coefIndex) {
            1 -> reference.nomk1 = name
            2 -> reference.nomk2 = name
            3 -> reference.nomk3 = name
            4 -> reference.nomk4 = name
            5 -> reference.nomk5 = name
        }
        _currentReference.value = reference.copy()
    }

    /**
     * Ajoute un nouveau coefficient à un groupe spécifique
     *
     * @param coefIndex L'index du groupe de coefficients (1 à 5)
     * @param description La description du coefficient
     * @param value La valeur du coefficient
     */
    fun addCoefficient(coefIndex: Int, description: String, value: Float) {
        val reference = _currentReference.value
        val newCoef = CoefP(description = description, coef = value, groupUUID = coefIndex - 1)

        when (coefIndex) {
            1 -> reference.modk1.add(newCoef)
            2 -> reference.modk2.add(newCoef)
            3 -> reference.modk3.add(newCoef)
            4 -> reference.modk4.add(newCoef)
            5 -> reference.modk5.add(newCoef)
        }

        _currentReference.value = reference.copy()
    }

    /**
     * Met à jour un coefficient existant
     *
     * @param coefIndex L'index du groupe de coefficients (1 à 5)
     * @param position La position du coefficient dans la liste
     * @param description La nouvelle description du coefficient
     * @param value La nouvelle valeur du coefficient
     */
    fun updateCoefficient(coefIndex: Int, position: Int, description: String, value: Float) {
        val reference = _currentReference.value

        val coefList =
                when (coefIndex) {
                    1 -> reference.modk1
                    2 -> reference.modk2
                    3 -> reference.modk3
                    4 -> reference.modk4
                    5 -> reference.modk5
                    else -> return
                }

        if (position in 0 until coefList.size) {
            val updatedCoef = coefList[position].copy(description = description, coef = value)
            coefList[position] = updatedCoef
        }

        _currentReference.value = reference.copy()
    }

    /**
     * Supprime un coefficient
     *
     * @param coefIndex L'index du groupe de coefficients (1 à 5)
     * @param position La position du coefficient dans la liste
     */
    fun removeCoefficient(coefIndex: Int, position: Int) {
        val reference = _currentReference.value

        when (coefIndex) {
            1 -> {
                if (position in 0 until reference.modk1.size) {
                    reference.modk1.removeAt(position)
                }
            }
            2 -> {
                if (position in 0 until reference.modk2.size) {
                    reference.modk2.removeAt(position)
                }
            }
            3 -> {
                if (position in 0 until reference.modk3.size) {
                    reference.modk3.removeAt(position)
                }
            }
            4 -> {
                if (position in 0 until reference.modk4.size) {
                    reference.modk4.removeAt(position)
                }
            }
            5 -> {
                if (position in 0 until reference.modk5.size) {
                    reference.modk5.removeAt(position)
                }
            }
        }

        _currentReference.value = reference.copy()
    }
}
