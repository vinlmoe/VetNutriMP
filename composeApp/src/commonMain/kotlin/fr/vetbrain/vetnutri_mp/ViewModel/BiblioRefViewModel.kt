package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateOf
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** ViewModel pour la gestion des références bibliographiques */
class BiblioRefViewModel(private val repository: BiblioRefRepository) {
    // Utilisation du dispatcher de la plateforme
    private val dispatcher = PlatformDispatcher().provideMainDispatcher()
    private val viewModelScope = CoroutineScope(dispatcher)

    // État de toutes les références bibliographiques
    val allBiblioRefs: StateFlow<List<BiblioRef>> =
            repository
                    .getAllBiblioRefs()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // État pour l'édition d'une référence bibliographique
    private val _currentBiblioRef = MutableStateFlow(BiblioRef())
    val currentBiblioRef: StateFlow<BiblioRef> = _currentBiblioRef

    // États pour les champs d'édition
    val firstAuthor = mutableStateOf("")
    val year = mutableStateOf("")
    val completeRef = mutableStateOf("")
    val comments = mutableStateOf("")
    val bibtex = mutableStateOf("")

    // État de validation du formulaire
    val isValid = mutableStateOf(false)

    // État de l'opération en cours
    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress

    // Message d'erreur ou de succès
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage

    /** Force le rechargement des références depuis le repository */
    fun refreshBiblioRefs() {
        viewModelScope.launch {
            try {
                println("DEBUG BiblioRefViewModel: Rafraîchissement forcé des références")
                // Utilisation d'une collecte avec timeout pour éviter de bloquer indéfiniment
                kotlinx.coroutines.withTimeoutOrNull(2000) {
                    repository.getAllBiblioRefs().collect { refs ->
                        println(
                                "DEBUG BiblioRefViewModel: ${refs.size} références récupérées lors du rafraîchissement"
                        )
                    }
                }
            } catch (e: Exception) {
                println("DEBUG BiblioRefViewModel: Erreur lors du rafraîchissement: ${e.message}")
            }
        }
    }

    /** Charge une référence bibliographique par son ID */
    suspend fun loadBiblioRefById(biblioRefId: String) {
        _actionInProgress.value = true

        try {
            val biblioRef = repository.getBiblioRefById(biblioRefId)

            if (biblioRef != null) {
                _currentBiblioRef.value = biblioRef

                // Initialiser les champs d'édition
                firstAuthor.value = biblioRef.firstAuthor
                year.value = if (biblioRef.year > 1800) biblioRef.year.toString() else ""
                completeRef.value = biblioRef.completeRef
                comments.value = biblioRef.comments
                bibtex.value = biblioRef.bibtex

                // Vérifier la validité
                validateForm()

                println(
                        "DEBUG BiblioRefViewModel: Référence chargée avec succès: ${biblioRef.firstAuthor}, ${biblioRef.year}"
                )
            } else {
                _operationMessage.value = "Référence non trouvée (ID: $biblioRefId)"
                println("DEBUG BiblioRefViewModel: Référence non trouvée (ID: $biblioRefId)")
            }
        } catch (e: Exception) {
            _operationMessage.value = "Erreur lors du chargement: ${e.message}"
            println("DEBUG BiblioRefViewModel: Erreur lors du chargement: ${e.message}")
            e.printStackTrace()
        } finally {
            _actionInProgress.value = false
        }
    }

    /** Initialise le ViewModel avec une nouvelle référence ou une référence existante */
    fun initForEdit(biblioRef: BiblioRef? = null) {
        val refToEdit = biblioRef ?: BiblioRef()
        _currentBiblioRef.value = refToEdit

        // Initialiser les champs d'édition
        firstAuthor.value = refToEdit.firstAuthor
        year.value = if (refToEdit.year > 1800) refToEdit.year.toString() else ""
        completeRef.value = refToEdit.completeRef
        comments.value = refToEdit.comments
        bibtex.value = refToEdit.bibtex

        // Vérifier la validité initiale
        validateForm()
    }

    /** Valide le formulaire */
    fun validateForm() {
        isValid.value =
                firstAuthor.value.isNotBlank() &&
                        year.value.isNotBlank() &&
                        completeRef.value.isNotBlank()
    }

    /** Met à jour la valeur du champ auteur */
    fun updateFirstAuthor(value: String) {
        firstAuthor.value = value
        validateForm()
    }

    /** Met à jour la valeur du champ année */
    fun updateYear(value: String) {
        // Accepter uniquement les chiffres
        if (value.isEmpty() || value.all { it.isDigit() }) {
            year.value = value
        }
        validateForm()
    }

    /** Met à jour la valeur du champ référence complète */
    fun updateCompleteRef(value: String) {
        completeRef.value = value
        validateForm()
    }

    /** Met à jour la valeur du champ commentaires */
    fun updateComments(value: String) {
        comments.value = value
    }

    /** Met à jour la valeur du champ bibtex */
    fun updateBibtex(value: String) {
        bibtex.value = value
    }

    /** Sauvegarde la référence bibliographique actuelle */
    fun saveBiblioRef() {
        if (!isValid.value) {
            println("DEBUG: Formulaire non valide, sauvegarde abandonnée")
            return
        }

        _actionInProgress.value = true

        // Créer une nouvelle instance de BiblioRef avec les valeurs du formulaire
        val biblioRefToSave =
                if (_currentBiblioRef.value.uuid.isBlank()) {
                    // Nouvelle référence avec un UUID généré
                    BiblioRef(
                            uuid = genUUID(),
                            firstAuthor = firstAuthor.value,
                            year = year.value.toIntOrNull() ?: 1800,
                            completeRef = completeRef.value,
                            comments = comments.value,
                            bibtex = bibtex.value,
                            consistent = 1 // Supposé cohérent lors de la création par l'utilisateur
                    )
                } else {
                    // Mise à jour d'une référence existante
                    BiblioRef(
                            uuid = _currentBiblioRef.value.uuid,
                            firstAuthor = firstAuthor.value,
                            year = year.value.toIntOrNull() ?: 1800,
                            completeRef = completeRef.value,
                            comments = comments.value,
                            bibtex = bibtex.value,
                            consistent =
                                    1 // Supposé cohérent lors de la mise à jour par l'utilisateur
                    )
                }

        // Log pour le débogage
        println(
                "DEBUG: Tentative de sauvegarde: ${biblioRefToSave.firstAuthor}, ${biblioRefToSave.year}, UUID: ${biblioRefToSave.uuid}"
        )

        viewModelScope.launch {
            try {
                // Déterminer s'il s'agit d'une création ou d'une mise à jour
                val isNewReference = _currentBiblioRef.value.uuid.isBlank()

                // Toujours utiliser insertBiblioRef qui gère correctement la création et la mise à
                // jour avec onConflictStrategy.REPLACE
                repository.insertBiblioRef(biblioRefToSave)

                if (isNewReference) {
                    _operationMessage.value = "Référence ajoutée avec succès"
                    println("DEBUG: Référence ajoutée: ${biblioRefToSave.firstAuthor}")
                } else {
                    _operationMessage.value = "Référence mise à jour avec succès"
                    println("DEBUG: Référence mise à jour: ${biblioRefToSave.firstAuthor}")
                }

                // Forcer le rafraîchissement des références pour mettre à jour la liste
                refreshBiblioRefs()

                // Réinitialiser le formulaire après sauvegarde
                initForEdit()
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                println("DEBUG: Erreur lors de la sauvegarde: ${e.message}")
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /** Supprime une référence bibliographique */
    fun deleteBiblioRef(biblioRef: BiblioRef) {
        _actionInProgress.value = true

        viewModelScope.launch {
            try {
                repository.deleteBiblioRef(biblioRef)
                _operationMessage.value = "Référence supprimée avec succès"

                // Réinitialiser le formulaire si la référence supprimée était en cours d'édition
                if (_currentBiblioRef.value.uuid == biblioRef.uuid) {
                    initForEdit()
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur: ${e.message}"
                println("DEBUG: Erreur lors de la suppression: ${e.message}")
                e.printStackTrace()
            } finally {
                _actionInProgress.value = false
            }
        }
    }

    /** Efface le message d'opération */
    fun clearOperationMessage() {
        _operationMessage.value = null
    }
}
