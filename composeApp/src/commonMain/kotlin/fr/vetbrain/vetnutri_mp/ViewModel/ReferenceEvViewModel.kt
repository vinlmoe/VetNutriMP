package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Repository.ReferenceEvRepository
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
        private val repository: ReferenceEvRepository,
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
        scope.launch {
            try {
                _loading.value = true
                _error.value = ""

                val references = repository.getAll()
                _allReferences.value = references
            } catch (e: Exception) {
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
    fun loadReferenceEvById(id: String) {
        scope.launch {
            _actionInProgress.value = true
            try {
                val referenceEv = repository.getById(id)

                if (referenceEv != null) {
                    _currentReferenceEv.value = referenceEv
                    _nom.value = referenceEv.nom
                    _description.value = referenceEv.description
                    _nomEnergie.value = referenceEv.nomEnergie
                    _espece.value = referenceEv.espece
                    _stadePhysio.value = referenceEv.stadePhysio
                    _isMaladie.value = referenceEv.maladie
                    _nomMaladie.value = referenceEv.nomMaladie
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement: ${e.message}"
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
}
