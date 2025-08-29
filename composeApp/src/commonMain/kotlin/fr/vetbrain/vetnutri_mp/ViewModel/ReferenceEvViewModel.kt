package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.CoefP
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** ViewModel pour la gestion des références évaluées (ReferenceEv). */
class ReferenceEvViewModel(
        private val repository: DatabaseReferenceEvRepository,
        private val platformDispatcher: PlatformDispatcher = PlatformDispatcher(),
        private val coroutineContext: CoroutineContext = platformDispatcher.provideMainDispatcher()
) {
    private val scope = CoroutineScope(coroutineContext)

    // Référence courante en édition
    private val _currentReferenceEv = MutableStateFlow(ReferenceEv())
    val currentReferenceEv: StateFlow<ReferenceEv> = _currentReferenceEv.asStateFlow()

    // État des champs du formulaire
    private val _nom = MutableStateFlow("")
    val nom: StateFlow<String> = _nom.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _nomEnergie = MutableStateFlow("")
    val nomEnergie: StateFlow<String> = _nomEnergie.asStateFlow()

    private val _espece = MutableStateFlow(Espece.CHIEN)
    val espece: StateFlow<Espece> = _espece.asStateFlow()

    private val _stadePhysio = MutableStateFlow(StadePhysio.ADULTE)
    val stadePhysio: StateFlow<StadePhysio> = _stadePhysio.asStateFlow()

    private val _isMaladie = MutableStateFlow(false)
    val isMaladie: StateFlow<Boolean> = _isMaladie.asStateFlow()

    private val _nomMaladie = MutableStateFlow("")
    val nomMaladie: StateFlow<String> = _nomMaladie.asStateFlow()

    // Validité du formulaire
    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    // Messages d'opération et état de chargement
    private val _operationMessage = MutableStateFlow("")
    val operationMessage: StateFlow<String> = _operationMessage.asStateFlow()

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    // États pour la liste des références
    private val _allReferences = MutableStateFlow<List<ReferenceEv>>(emptyList())
    val allReferences: StateFlow<List<ReferenceEv>> = _allReferences.asStateFlow()

    // État pour la recherche
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    // État pour le filtre par espèce
    private val _selectedEspece = MutableStateFlow<Espece?>(null)
    val selectedEspece: StateFlow<Espece?> = _selectedEspece.asStateFlow()

    // États pour l'état de chargement et les erreurs
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    // États pour les équations disponibles
    private val _loadingEquations = MutableStateFlow(false)
    val loadingEquations: StateFlow<Boolean> = _loadingEquations.asStateFlow()

    private val _availableEquations = MutableStateFlow<List<Equation>>(emptyList())
    val availableEquations: StateFlow<List<Equation>> = _availableEquations.asStateFlow()

    // Références filtrées en fonction des critères de recherche
    val filteredReferences =
            combine(allReferences, searchText, selectedEspece) { list, search, espece ->
                var filtered = list

                // Filtre par texte de recherche
                if (search.isNotBlank()) {
                    val searchLower = search.lowercase()
                    filtered =
                            filtered.filter { reference ->
                                reference.nom.lowercase().contains(searchLower) ||
                                        reference.description.lowercase().contains(searchLower) ||
                                        reference.nomEnergie.lowercase().contains(searchLower) ||
                                        (reference.maladie &&
                                                reference
                                                        .nomMaladie
                                                        .lowercase()
                                                        .contains(searchLower))
                            }
                }

                // Filtre par espèce
                if (espece != null) {
                    filtered = filtered.filter { it.espece == espece }
                }

                filtered
            }

    init {
        // Charger les références au démarrage
        loadAllReferences()
    }

    /** Charge toutes les références depuis le repository */
    fun loadAllReferences() {
        println("🔄 [LOAD] Chargement de toutes les références...")
        scope.launch {
            try {
                _loading.value = true
                _error.value = ""

                val references = repository.getAll()
                println("🔄 [LOAD] ${references.size} références chargées")
                references.forEach { ref ->
                    println("🔄 [LOAD] Référence trouvée: ${ref.nom} (UUID: ${ref.uuid})")
                }
                _allReferences.value = references
                println("✅ [LOAD] Chargement terminé avec succès")
            } catch (e: Exception) {
                println("❌ [LOAD] Erreur lors du chargement: ${e.message}")
                _error.value =
                        "Erreur lors du chargement des références: ${e.message ?: "Erreur inconnue"}"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Met à jour le texte de recherche */
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    /** Met à jour l'espèce sélectionnée pour le filtrage */
    fun updateSelectedEspece(espece: Espece?) {
        _selectedEspece.value = espece
    }

    /** Crée une nouvelle référence */
    suspend fun createReference(
            nom: String,
            description: String,
            espece: Espece,
            stadePhysio: StadePhysio,
            nomEnergie: String,
            maladie: Boolean = false,
            nomMaladie: String = ""
    ): ReferenceEv {
        _loading.value = true

        try {
            val newReference = ReferenceEv()
            newReference.nom = nom
            newReference.description = description
            newReference.espece = espece
            newReference.stadePhysio = stadePhysio
            newReference.nomEnergie = nomEnergie
            newReference.maladie = maladie
            newReference.nomMaladie = nomMaladie

            repository.create(newReference)
            loadAllReferences() // Recharger la liste

            return newReference
        } catch (e: Exception) {
            _error.value =
                    "Erreur lors de la création de la référence: ${e.message ?: "Erreur inconnue"}"
            throw e
        } finally {
            _loading.value = false
        }
    }

    /** Met à jour une référence existante */
    suspend fun updateReference(reference: ReferenceEv) {
        _loading.value = true

        try {
            repository.update(reference)
            loadAllReferences() // Recharger la liste
        } catch (e: Exception) {
            _error.value =
                    "Erreur lors de la mise à jour de la référence: ${e.message ?: "Erreur inconnue"}"
            throw e
        } finally {
            _loading.value = false
        }
    }

    /** Supprime une référence */
    suspend fun deleteReference(referenceId: String) {
        _loading.value = true

        try {
            repository.delete(referenceId)
            loadAllReferences() // Recharger la liste
        } catch (e: Exception) {
            _error.value =
                    "Erreur lors de la suppression de la référence: ${e.message ?: "Erreur inconnue"}"
            throw e
        } finally {
            _loading.value = false
        }
    }

    /** Duplique une référence en base (copie intégrale sans conserver l'UUID) */
    suspend fun duplicateReference(source: ReferenceEv) {
        println("🔄 [DUPLICATION] Début de la duplication pour référence: ${source.nom} (UUID: ${source.uuid})")
        _loading.value = true
        try {
            // Créer une nouvelle référence avec un nouvel UUID
            val newUuid = fr.vetbrain.vetnutri_mp.Utils.genUUID()
            println("🔄 [DUPLICATION] Nouveau UUID généré: $newUuid")

            val duplicated = ReferenceEv(
                uuid = newUuid,
                nom = source.nom + " (Copie)",
                description = source.description,
                maladie = source.maladie,
                nomMaladie = source.nomMaladie,
                nomEnergie = source.nomEnergie,
                consistent = source.consistent,
                espece = source.espece,
                stadePhysio = source.stadePhysio
            )

            println("🔄 [DUPLICATION] Nouvelle référence créée: ${duplicated.nom} (UUID: ${duplicated.uuid})")

            // Référencer les équations associées (pas de duplication)
            println("🔄 [DUPLICATION] Référencement des équations principales...")
            duplicated.equationBW = source.equationBW?.let {
                println("🔄 [DUPLICATION] Référence equationBW: ${it.name} (UUID: ${it.uuid})")
                it
            }
            duplicated.equationBEE = source.equationBEE?.let {
                println("🔄 [DUPLICATION] Référence equationBEE: ${it.name} (UUID: ${it.uuid})")
                it
            }
            duplicated.equationDEcom = source.equationDEcom?.let {
                println("🔄 [DUPLICATION] Référence equationDEcom: ${it.name} (UUID: ${it.uuid})")
                it
            }
            duplicated.equationDEraw = source.equationDEraw?.let {
                println("🔄 [DUPLICATION] Référence equationDEraw: ${it.name} (UUID: ${it.uuid})")
                it
            }
            duplicated.equationME = source.equationME?.let {
                println("🔄 [DUPLICATION] Référence equationME: ${it.name} (UUID: ${it.uuid})")
                it
            }

            // Référencer les équations nutritionnelles (pas de duplication)
            println("🔄 [DUPLICATION] Référencement des équations nutritionnelles: ${source.equationsNut.size} équations")
            duplicated.equationsNut = ArrayList(source.equationsNut.map { equation ->
                println("🔄 [DUPLICATION] Référence equation nutritionnelle: ${equation.name} (UUID: ${equation.uuid})")
                equation
            })

            // Référencer les nutriments avec leurs références bibliographiques existantes
            println("🔄 [DUPLICATION] Référencement des nutriments...")
            val minCount = source.getRefMapMin().size
            val maxCount = source.getRefMapMax().size
            val ominCount = source.getRefMapOMin().size
            val omaxCount = source.getRefMapOMax().size
            println("🔄 [DUPLICATION] Nutriments à référencer - MIN: $minCount, MAX: $maxCount, OPTIMIN: $ominCount, OPTIMAX: $omaxCount")

            // MIN
            source.getRefMapMin().forEach { (nutrient, nutRef) ->
                println("🔄 [DUPLICATION] Référence nutriment MIN: ${nutrient.label} = ${nutRef.quantite} (biblio: ${nutRef.biblio.firstAuthor})")
                duplicated.definirNutriment(
                    valeur = nutRef.quantite,
                    nutrient = nutrient,
                    niveauRef = nutRef.niveauRelatif,
                    uniteReq = nutRef.uniteReq,
                    biblio = nutRef.biblio, // Référence directe, pas de clonage
                    unitEnum = nutRef.unite
                )
            }
            // MAX
            source.getRefMapMax().forEach { (nutrient, nutRef) ->
                println("🔄 [DUPLICATION] Référence nutriment MAX: ${nutrient.label} = ${nutRef.quantite} (biblio: ${nutRef.biblio.firstAuthor})")
                duplicated.definirNutriment(
                    valeur = nutRef.quantite,
                    nutrient = nutrient,
                    niveauRef = nutRef.niveauRelatif,
                    uniteReq = nutRef.uniteReq,
                    biblio = nutRef.biblio, // Référence directe, pas de clonage
                    unitEnum = nutRef.unite
                )
            }
            // OPTIMIN
            source.getRefMapOMin().forEach { (nutrient, nutRef) ->
                println("🔄 [DUPLICATION] Référence nutriment OPTIMIN: ${nutrient.label} = ${nutRef.quantite} (biblio: ${nutRef.biblio.firstAuthor})")
                duplicated.definirNutriment(
                    valeur = nutRef.quantite,
                    nutrient = nutrient,
                    niveauRef = nutRef.niveauRelatif,
                    uniteReq = nutRef.uniteReq,
                    biblio = nutRef.biblio, // Référence directe, pas de clonage
                    unitEnum = nutRef.unite
                )
            }
            // OPTIMAX
            source.getRefMapOMax().forEach { (nutrient, nutRef) ->
                println("🔄 [DUPLICATION] Référence nutriment OPTIMAX: ${nutrient.label} = ${nutRef.quantite} (biblio: ${nutRef.biblio.firstAuthor})")
                duplicated.definirNutriment(
                    valeur = nutRef.quantite,
                    nutrient = nutrient,
                    niveauRef = nutRef.niveauRelatif,
                    uniteReq = nutRef.uniteReq,
                    biblio = nutRef.biblio, // Référence directe, pas de clonage
                    unitEnum = nutRef.unite
                )
            }

            // Référencer les coefficients modificateurs (pas de duplication)
            println("🔄 [DUPLICATION] Référencement des coefficients modificateurs...")
            val k1Count = source.getModk1().size
            val k2Count = source.getModk2().size
            val k3Count = source.getModk3().size
            val k4Count = source.getModk4().size
            val k5Count = source.getModk5().size
            println("🔄 [DUPLICATION] Coefficients à référencer - K1: $k1Count, K2: $k2Count, K3: $k3Count, K4: $k4Count, K5: $k5Count")

            duplicated.getModk1().clear()
            duplicated.getModk1().addAll(source.getModk1()) // Référence directe
            duplicated.getModk2().clear()
            duplicated.getModk2().addAll(source.getModk2()) // Référence directe
            duplicated.getModk3().clear()
            duplicated.getModk3().addAll(source.getModk3()) // Référence directe
            duplicated.getModk4().clear()
            duplicated.getModk4().addAll(source.getModk4()) // Référence directe
            duplicated.getModk5().clear()
            duplicated.getModk5().addAll(source.getModk5()) // Référence directe

            // Copier les noms des coefficients
            duplicated.nomk1 = source.nomk1
            duplicated.nomk2 = source.nomk2
            duplicated.nomk3 = source.nomk3
            duplicated.nomk4 = source.nomk4
            duplicated.nomk5 = source.nomk5

            println("🔄 [DUPLICATION] Sauvegarde dans la base de données...")
            val saveResult = repository.create(duplicated)
            println("🔄 [DUPLICATION] Résultat de la sauvegarde: $saveResult")

            if (saveResult) {
                println("🔄 [DUPLICATION] Rechargement de la liste des références...")
                loadAllReferences()
                _operationMessage.value = "Référence dupliquée avec succès"
                println("✅ [DUPLICATION] Duplication terminée avec succès")
            } else {
                _operationMessage.value = "Erreur lors de la sauvegarde de la référence dupliquée"
                println("❌ [DUPLICATION] Erreur lors de la sauvegarde")
            }
        } catch (e: Exception) {
            _error.value = "Erreur lors de la duplication: ${e.message ?: "Erreur inconnue"}"
        } finally {
            _loading.value = false
        }
    }

    // Méthode cloneEquation supprimée - on référence directement les équations existantes

    // Méthode cloneBiblioRef supprimée - on référence directement les bibliographies existantes

    // Méthode cloneCoefP supprimée - on référence directement les coefficients existants

    /** Obtient une référence par son identifiant */
    suspend fun getReferenceById(referenceId: String): ReferenceEv? {
        return try {
            repository.getById(referenceId)
        } catch (e: Exception) {
            _error.value =
                    "Erreur lors de la récupération de la référence: ${e.message ?: "Erreur inconnue"}"
            null
        }
    }

    // Initialiser pour l'édition
    fun initForEdit() {
        _currentReferenceEv.value = ReferenceEv()
        _nom.value = ""
        _description.value = ""
        _nomEnergie.value = ""
        _espece.value = Espece.CHIEN
        _stadePhysio.value = StadePhysio.ADULTE
        _isMaladie.value = false
        _nomMaladie.value = ""

        validateForm()
    }

    // Charger une référence existante par son ID
    fun loadReferenceEvById(referenceEvId: String) {
        _actionInProgress.value = true

        scope.launch {
            try {
                val referenceEv = repository.getById(referenceEvId)

                // Récupération des équations associées
                // L'appel suivant semble ne pas exister, commentons-le pour l'instant
                // val associatedEquations = repository.getEquationsForReference(referenceEvId)
                // 
                // associatedEquations.forEach { equation ->
                //     
                // }

                // Mise à jour des états si la référence est trouvée
                referenceEv?.let { ref ->
                    // D'abord, copions les attributs de base de la référence
                    _currentReferenceEv.value =
                            _currentReferenceEv.value.copy(
                                    uuid = ref.uuid,
                                    nom = ref.nom,
                                    description = ref.description,
                                    espece = ref.espece,
                                    stadePhysio = ref.stadePhysio,
                                    maladie = ref.maladie,
                                    nomMaladie = ref.nomMaladie,
                                    nomEnergie = ref.nomEnergie,
                                    consistent = ref.consistent
                            )

                    // Maintenant, assignons chaque équation séparément
                    val updatedReference = _currentReferenceEv.value
                    updatedReference.equationBW = ref.equationBW
                    updatedReference.equationBEE = ref.equationBEE
                    updatedReference.equationDEcom = ref.equationDEcom
                    updatedReference.equationDEraw = ref.equationDEraw
                    updatedReference.equationsNut = ref.equationsNut
                    _currentReferenceEv.value = updatedReference

                    // Mettons à jour les autres états
                    _nom.value = ref.nom
                    _description.value = ref.description
                    _nomEnergie.value = ref.nomEnergie
                    _espece.value = ref.espece
                    _stadePhysio.value = ref.stadePhysio
                    _isMaladie.value = ref.maladie
                    _nomMaladie.value = ref.nomMaladie

                    _operationMessage.value = "Référence chargée avec succès"
                }
                        ?: run {
                            _operationMessage.value =
                                    "Référence non trouvée avec l'ID: $referenceEvId"
                        }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement de la référence: ${e.message}"
                _operationMessage.value = "Erreur lors du chargement de la référence: ${e.message}"
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    // Mettre à jour les champs du formulaire
    fun updateNom(value: String) {
        _nom.value = value
        validateForm()
    }

    fun updateDescription(value: String) {
        _description.value = value
        validateForm()
    }

    fun updateNomEnergie(value: String) {
        _nomEnergie.value = value
        validateForm()
    }

    fun updateEspece(value: Espece) {
        _espece.value = value
        validateForm()
    }

    fun updateStadePhysio(value: StadePhysio) {
        _stadePhysio.value = value
        validateForm()
    }

    fun updateIsMaladie(value: Boolean) {
        _isMaladie.value = value
        validateForm()
    }

    fun updateNomMaladie(value: String) {
        _nomMaladie.value = value
        validateForm()
    }

    // Valider le formulaire
    private fun validateForm() {
        _isValid.value =
                _nom.value.isNotBlank() &&
                        (!_isMaladie.value || (_isMaladie.value && _nomMaladie.value.isNotBlank()))
    }

    // Sauvegarder la référence
    suspend fun saveReferenceEv() {
        _actionInProgress.value = true
        try {
            val referenceEv = ReferenceEv()
            referenceEv.nom = _nom.value
            referenceEv.description = _description.value
            referenceEv.nomEnergie = _nomEnergie.value
            referenceEv.espece = _espece.value
            referenceEv.stadePhysio = _stadePhysio.value
            referenceEv.maladie = _isMaladie.value
            referenceEv.nomMaladie = if (_isMaladie.value) _nomMaladie.value else ""

            if (_currentReferenceEv.value.uuid.isBlank()) {
                // Création
                val newReference =
                        createReference(
                                nom = referenceEv.nom,
                                description = referenceEv.description,
                                espece = referenceEv.espece,
                                stadePhysio = referenceEv.stadePhysio,
                                nomEnergie = referenceEv.nomEnergie,
                                maladie = referenceEv.maladie,
                                nomMaladie = referenceEv.nomMaladie
                        )
                _currentReferenceEv.value = newReference
            } else {
                // Mise à jour
                val updatedReference =
                        _currentReferenceEv.value.copy(
                                nom = referenceEv.nom,
                                description = referenceEv.description,
                                espece = referenceEv.espece,
                                stadePhysio = referenceEv.stadePhysio,
                                nomEnergie = referenceEv.nomEnergie,
                                maladie = referenceEv.maladie,
                                nomMaladie = referenceEv.nomMaladie
                        )
                updateReference(updatedReference)
            }

            _operationMessage.value = "Référence sauvegardée avec succès"
        } catch (e: Exception) {
            _operationMessage.value = "Erreur lors de la sauvegarde: ${e.message}"
            throw e
        } finally {
            _actionInProgress.value = false
        }
    }

    // Effacer le message d'opération
    fun clearOperationMessage() {
        _operationMessage.value = ""
    }

    /** Met à jour une équation spécifique pour la référence en cours */
    fun updateReferenceEquation(type: String, equationId: String?) {
        val currentRef = _currentReferenceEv.value

        scope.launch {
            _actionInProgress.value = true
            try {
                // Clone de la référence actuelle pour modification
                val updatedReference = currentRef.copy()

                // Rechercher l'équation correspondante dans la liste disponible
                val selectedEquation =
                        if (equationId != null) {
                            _availableEquations.value.find { it.uuid == equationId }
                        } else {
                            null
                        }

                // Mise à jour de l'équation selon son type
                when (type) {
                    "BW" -> updatedReference.equationBW = selectedEquation ?: Equation()
                    "BEE" -> updatedReference.equationBEE = selectedEquation ?: Equation()
                    "DEcom" -> updatedReference.equationDEcom = selectedEquation ?: Equation()
                    "DEraw" -> updatedReference.equationDEraw = selectedEquation ?: Equation()
                }

                // Mise à jour dans la base de données
                val result = repository.update(updatedReference)
                if (result) {
                    // Mise à jour réussie, mise à jour de l'état
                    _currentReferenceEv.value = updatedReference
                    _operationMessage.value = "Équation mise à jour avec succès"
                } else {
                    _operationMessage.value = "Erreur lors de la mise à jour de l'équation"
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /** Associe ou dissocie une équation nutritionnelle à la référence en cours */
    fun toggleNutritionEquation(equation: Equation) {
        val currentRef = _currentReferenceEv.value

        // Afficher les équations actuellement associées
        currentRef.equationsNut.forEach { equationItem -> }

        scope.launch {
            _actionInProgress.value = true
            try {
                // Vérifier si l'équation est déjà associée
                val isAlreadyAssociated = currentRef.equationsNut.any { it.uuid == equation.uuid }

                // Clone de la référence actuelle pour modification
                val updatedReference = currentRef.copy()

                // Mise à jour de la liste des équations nutritionnelles
                if (isAlreadyAssociated) {
                    // Dissocier l'équation
                    updatedReference.equationsNut =
                            ArrayList(
                                    updatedReference.equationsNut.filter {
                                        it.uuid != equation.uuid
                                    }
                            )
                } else {
                    // Associer l'équation (créer une nouvelle liste pour éviter des problèmes de
                    // référence)
                    val newList = ArrayList(updatedReference.equationsNut)
                    newList.add(equation)
                    updatedReference.equationsNut = newList
                }

                // Mise à jour dans la base de données
                val result = repository.update(updatedReference)
                if (result) {
                    // Mise à jour réussie, mise à jour de l'état
                    _currentReferenceEv.value = updatedReference
                    val action = if (isAlreadyAssociated) "dissociée" else "associée"
                    _operationMessage.value = "Équation $action avec succès"
                } else {
                    _operationMessage.value =
                            "Erreur lors de la modification des équations nutritionnelles"
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    fun loadEquations() {
        _loadingEquations.value = true

        scope.launch {
            try {
                // Création d'équations de démonstration pour le développement
                val equations =
                        listOf(
                                Equation(
                                        uuid = "equation-1",
                                        name = "BEE Chien",
                                        kind =
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                        .ENERGYDENSITY
                                ),
                                Equation(
                                        uuid = "equation-2",
                                        name = "BEE Chat",
                                        kind =
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                        .ENERGYNEED
                                ),
                                Equation(
                                        uuid = "equation-3",
                                        name = "Poids métabolique",
                                        kind = fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW
                                ),
                                Equation(
                                        uuid = "equation-4",
                                        name = "Densité énergétique",
                                        kind =
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                        .ENERGYDENSITY
                                )
                        )

                _availableEquations.value = equations

                // Vérification des équations disponibles
                for (i in equations.indices) {
                    val equation = equations[i]
                }

                // Vérification des équations associées à la référence courante
                _currentReferenceEv.value.equationsNut.forEach { equation -> }

                // Vérification des équations principales
            } catch (e: Exception) {
                e.printStackTrace()
                _operationMessage.value = "Erreur lors du chargement des équations: ${e.message}"
            } finally {
                _loadingEquations.value = false
            }
        }
    }
}
