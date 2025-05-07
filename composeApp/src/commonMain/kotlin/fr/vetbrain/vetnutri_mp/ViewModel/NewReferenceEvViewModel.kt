package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.CoefP
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

    // États pour les listes de coefficients
    private val _coefList1 = MutableStateFlow<List<CoefP>>(emptyList())
    val coefList1: StateFlow<List<CoefP>> = _coefList1.asStateFlow()

    private val _coefList2 = MutableStateFlow<List<CoefP>>(emptyList())
    val coefList2: StateFlow<List<CoefP>> = _coefList2.asStateFlow()

    private val _coefList3 = MutableStateFlow<List<CoefP>>(emptyList())
    val coefList3: StateFlow<List<CoefP>> = _coefList3.asStateFlow()

    private val _coefList4 = MutableStateFlow<List<CoefP>>(emptyList())
    val coefList4: StateFlow<List<CoefP>> = _coefList4.asStateFlow()

    private val _coefList5 = MutableStateFlow<List<CoefP>>(emptyList())
    val coefList5: StateFlow<List<CoefP>> = _coefList5.asStateFlow()

    // Noms des groupes de coefficients
    private val _coefName1 = MutableStateFlow("")
    val coefName1: StateFlow<String> = _coefName1.asStateFlow()

    private val _coefName2 = MutableStateFlow("")
    val coefName2: StateFlow<String> = _coefName2.asStateFlow()

    private val _coefName3 = MutableStateFlow("")
    val coefName3: StateFlow<String> = _coefName3.asStateFlow()

    private val _coefName4 = MutableStateFlow("")
    val coefName4: StateFlow<String> = _coefName4.asStateFlow()

    private val _coefName5 = MutableStateFlow("")
    val coefName5: StateFlow<String> = _coefName5.asStateFlow()

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

        // Initialisation des coefficients
        initCoefficients()
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

                // Mise à jour des coefficients à partir de la référence
                loadCoefficientsFromReference(reference)
            } else {
                _errorMessage.value = "Référence non trouvée"
                _currentReference.value = ReferenceEv()
                _isEditMode.value = false

                // Initialisation des coefficients par défaut
                initCoefficients()
            }
            loadEquations()
            loadBiblioRefs()
        }
    }

    /** Initialise les coefficients avec les valeurs par défaut */
    private fun initCoefficients() {
        // Initialiser avec un coefficient "Normal" pour chaque groupe
        _coefList1.value = listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 0))
        _coefList2.value = listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 1))
        _coefList3.value = listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 2))
        _coefList4.value = listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 3))
        _coefList5.value = listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 4))

        // Initialiser les noms des groupes
        _coefName1.value = ""
        _coefName2.value = ""
        _coefName3.value = ""
        _coefName4.value = ""
        _coefName5.value = ""
    }

    /**
     * Charge les coefficients à partir d'une référence
     *
     * @param reference La référence contenant les coefficients
     */
    private fun loadCoefficientsFromReference(reference: ReferenceEv) {
        _coefName1.value = reference.nomk1
        _coefName2.value = reference.nomk2
        _coefName3.value = reference.nomk3
        _coefName4.value = reference.nomk4
        _coefName5.value = reference.nomk5

        // Accès aux propriétés privées via la réflexion
        try {
            val modk1Field = ReferenceEv::class.java.getDeclaredField("modk1")
            modk1Field.isAccessible = true
            val modk1 = modk1Field.get(reference) as? ArrayList<CoefP>
            _coefList1.value =
                    modk1?.toList()
                            ?: listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 0))

            val modk2Field = ReferenceEv::class.java.getDeclaredField("modk2")
            modk2Field.isAccessible = true
            val modk2 = modk2Field.get(reference) as? ArrayList<CoefP>
            _coefList2.value =
                    modk2?.toList()
                            ?: listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 1))

            val modk3Field = ReferenceEv::class.java.getDeclaredField("modk3")
            modk3Field.isAccessible = true
            val modk3 = modk3Field.get(reference) as? ArrayList<CoefP>
            _coefList3.value =
                    modk3?.toList()
                            ?: listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 2))

            val modk4Field = ReferenceEv::class.java.getDeclaredField("modk4")
            modk4Field.isAccessible = true
            val modk4 = modk4Field.get(reference) as? ArrayList<CoefP>
            _coefList4.value =
                    modk4?.toList()
                            ?: listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 3))

            val modk5Field = ReferenceEv::class.java.getDeclaredField("modk5")
            modk5Field.isAccessible = true
            val modk5 = modk5Field.get(reference) as? ArrayList<CoefP>
            _coefList5.value =
                    modk5?.toList()
                            ?: listOf(CoefP(description = "Normal", coef = 1.0f, groupUUID = 4))
        } catch (e: Exception) {
            println("Erreur lors du chargement des coefficients: ${e.message}")
            initCoefficients()
        }
    }

    /**
     * Met à jour le nom d'un groupe de coefficients
     *
     * @param groupIndex L'index du groupe (1-5)
     * @param name Le nouveau nom du groupe
     */
    fun updateCoefGroupName(groupIndex: Int, name: String) {
        val reference = _currentReference.value
        when (groupIndex) {
            1 -> {
                _coefName1.value = name
                reference.nomk1 = name
            }
            2 -> {
                _coefName2.value = name
                reference.nomk2 = name
            }
            3 -> {
                _coefName3.value = name
                reference.nomk3 = name
            }
            4 -> {
                _coefName4.value = name
                reference.nomk4 = name
            }
            5 -> {
                _coefName5.value = name
                reference.nomk5 = name
            }
        }
        _currentReference.value = reference
    }

    /**
     * Ajoute un nouveau coefficient à un groupe
     *
     * @param groupIndex L'index du groupe (1-5)
     */
    fun addCoef(groupIndex: Int) {
        when (groupIndex) {
            1 -> {
                val currentList = _coefList1.value.toMutableList()
                currentList.add(CoefP(description = "Nouveau", coef = 1.0f, groupUUID = 0))
                _coefList1.value = currentList
            }
            2 -> {
                val currentList = _coefList2.value.toMutableList()
                currentList.add(CoefP(description = "Nouveau", coef = 1.0f, groupUUID = 1))
                _coefList2.value = currentList
            }
            3 -> {
                val currentList = _coefList3.value.toMutableList()
                currentList.add(CoefP(description = "Nouveau", coef = 1.0f, groupUUID = 2))
                _coefList3.value = currentList
            }
            4 -> {
                val currentList = _coefList4.value.toMutableList()
                currentList.add(CoefP(description = "Nouveau", coef = 1.0f, groupUUID = 3))
                _coefList4.value = currentList
            }
            5 -> {
                val currentList = _coefList5.value.toMutableList()
                currentList.add(CoefP(description = "Nouveau", coef = 1.0f, groupUUID = 4))
                _coefList5.value = currentList
            }
        }
    }

    /**
     * Supprime un coefficient d'un groupe
     *
     * @param groupIndex L'index du groupe (1-5)
     * @param coefIndex L'index du coefficient à supprimer
     */
    fun removeCoef(groupIndex: Int, coefIndex: Int) {
        when (groupIndex) {
            1 -> {
                val currentList = _coefList1.value.toMutableList()
                if (currentList.size > 1 && coefIndex >= 0 && coefIndex < currentList.size) {
                    currentList.removeAt(coefIndex)
                    _coefList1.value = currentList
                }
            }
            2 -> {
                val currentList = _coefList2.value.toMutableList()
                if (currentList.size > 1 && coefIndex >= 0 && coefIndex < currentList.size) {
                    currentList.removeAt(coefIndex)
                    _coefList2.value = currentList
                }
            }
            3 -> {
                val currentList = _coefList3.value.toMutableList()
                if (currentList.size > 1 && coefIndex >= 0 && coefIndex < currentList.size) {
                    currentList.removeAt(coefIndex)
                    _coefList3.value = currentList
                }
            }
            4 -> {
                val currentList = _coefList4.value.toMutableList()
                if (currentList.size > 1 && coefIndex >= 0 && coefIndex < currentList.size) {
                    currentList.removeAt(coefIndex)
                    _coefList4.value = currentList
                }
            }
            5 -> {
                val currentList = _coefList5.value.toMutableList()
                if (currentList.size > 1 && coefIndex >= 0 && coefIndex < currentList.size) {
                    currentList.removeAt(coefIndex)
                    _coefList5.value = currentList
                }
            }
        }
    }

    /**
     * Met à jour la description d'un coefficient
     *
     * @param groupIndex L'index du groupe (1-5)
     * @param coefIndex L'index du coefficient à mettre à jour
     * @param description La nouvelle description
     */
    fun updateCoefDescription(groupIndex: Int, coefIndex: Int, description: String) {
        when (groupIndex) {
            1 -> {
                val currentList = _coefList1.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(description = description)
                    _coefList1.value = currentList
                }
            }
            2 -> {
                val currentList = _coefList2.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(description = description)
                    _coefList2.value = currentList
                }
            }
            3 -> {
                val currentList = _coefList3.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(description = description)
                    _coefList3.value = currentList
                }
            }
            4 -> {
                val currentList = _coefList4.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(description = description)
                    _coefList4.value = currentList
                }
            }
            5 -> {
                val currentList = _coefList5.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(description = description)
                    _coefList5.value = currentList
                }
            }
        }
    }

    /**
     * Met à jour la valeur d'un coefficient
     *
     * @param groupIndex L'index du groupe (1-5)
     * @param coefIndex L'index du coefficient à mettre à jour
     * @param value La nouvelle valeur
     */
    fun updateCoefValue(groupIndex: Int, coefIndex: Int, value: Float) {
        when (groupIndex) {
            1 -> {
                val currentList = _coefList1.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(coef = value)
                    _coefList1.value = currentList
                }
            }
            2 -> {
                val currentList = _coefList2.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(coef = value)
                    _coefList2.value = currentList
                }
            }
            3 -> {
                val currentList = _coefList3.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(coef = value)
                    _coefList3.value = currentList
                }
            }
            4 -> {
                val currentList = _coefList4.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(coef = value)
                    _coefList4.value = currentList
                }
            }
            5 -> {
                val currentList = _coefList5.value.toMutableList()
                if (coefIndex >= 0 && coefIndex < currentList.size) {
                    val coef = currentList[coefIndex]
                    currentList[coefIndex] = coef.copy(coef = value)
                    _coefList5.value = currentList
                }
            }
        }
    }

    /** Applique les coefficients à la référence actuelle avant la sauvegarde */
    fun applyCoefficientsToReference() {
        val reference = _currentReference.value

        // Accès aux propriétés privées via la réflexion
        try {
            val modk1Field = ReferenceEv::class.java.getDeclaredField("modk1")
            modk1Field.isAccessible = true
            val modk1 = modk1Field.get(reference) as ArrayList<CoefP>
            modk1.clear()
            modk1.addAll(_coefList1.value)

            val modk2Field = ReferenceEv::class.java.getDeclaredField("modk2")
            modk2Field.isAccessible = true
            val modk2 = modk2Field.get(reference) as ArrayList<CoefP>
            modk2.clear()
            modk2.addAll(_coefList2.value)

            val modk3Field = ReferenceEv::class.java.getDeclaredField("modk3")
            modk3Field.isAccessible = true
            val modk3 = modk3Field.get(reference) as ArrayList<CoefP>
            modk3.clear()
            modk3.addAll(_coefList3.value)

            val modk4Field = ReferenceEv::class.java.getDeclaredField("modk4")
            modk4Field.isAccessible = true
            val modk4 = modk4Field.get(reference) as ArrayList<CoefP>
            modk4.clear()
            modk4.addAll(_coefList4.value)

            val modk5Field = ReferenceEv::class.java.getDeclaredField("modk5")
            modk5Field.isAccessible = true
            val modk5 = modk5Field.get(reference) as ArrayList<CoefP>
            modk5.clear()
            modk5.addAll(_coefList5.value)
        } catch (e: Exception) {
            println("Erreur lors de l'application des coefficients: ${e.message}")
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

        // Appliquer les coefficients à la référence avant la sauvegarde
        applyCoefficientsToReference()

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
                    // Ne pas définir _operationSuccess à true pour éviter de quitter la vue
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation de poids corporel"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
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
                    // Ne pas définir _operationSuccess à true pour éviter de quitter la vue
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation de besoin énergétique basal"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
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
                    // Ne pas définir _operationSuccess à true pour éviter de quitter la vue
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation d'énergie digestible pour aliments composés"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
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
                    // Ne pas définir _operationSuccess à true pour éviter de quitter la vue
                    _errorMessage.value = null
                } else {
                    _errorMessage.value =
                            "Erreur lors de la mise à jour de l'équation d'énergie digestible pour aliments bruts"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur: ${e.message}"
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
}
